import { ModelWeights, TrainingSample, TrainingProgress } from '../types';

export class OnDeviceTrainableModel {
  public inputDim: number;
  public hiddenDim: number;
  public outputDim: number;
  public weights: ModelWeights;

  public static readonly MEANS = [4.2, 8.0, 45.7, 2.0, 3.8];
  public static readonly STDS = [2.0, 4.1, 28.5, 0.8, 1.9];

  constructor(inputDim = 5, hiddenDim = 8, outputDim = 4, initialWeights?: ModelWeights) {
    this.inputDim = inputDim;
    this.hiddenDim = hiddenDim;
    this.outputDim = outputDim;
    this.weights = initialWeights || OnDeviceTrainableModel.defaultInit(inputDim, hiddenDim, outputDim);
  }

  public static normalize(rawInput: number[]): number[] {
    const out = new Array(rawInput.length);
    for (let i = 0; i < rawInput.length; i++) {
      const m = i < this.MEANS.length ? this.MEANS[i] : 0;
      const s = i < this.STDS.length && this.STDS[i] > 0 ? this.STDS[i] : 1;
      const val = isNaN(rawInput[i]) || !isFinite(rawInput[i]) ? 0 : rawInput[i];
      out[i] = (val - m) / s;
    }
    return out;
  }

  public static defaultInit(inputDim = 5, hiddenDim = 8, outputDim = 4): ModelWeights {
    // Seeded pseudo-random initialization
    let seed = 42;
    const rnd = () => {
      seed = (seed * 9301 + 49297) % 233280;
      return seed / 233280;
    };

    const scale1 = Math.sqrt(2.0 / inputDim);
    const w1: number[][] = [];
    for (let i = 0; i < hiddenDim; i++) {
      const row: number[] = [];
      for (let j = 0; j < inputDim; j++) {
        row.push((rnd() * 2 - 1) * scale1);
      }
      w1.push(row);
    }
    const b1 = new Array(hiddenDim).fill(0.05);

    const scale2 = Math.sqrt(2.0 / hiddenDim);
    const w2: number[][] = [];
    for (let k = 0; k < outputDim; k++) {
      const row: number[] = [];
      for (let i = 0; i < hiddenDim; i++) {
        row.push((rnd() * 2 - 1) * scale2);
      }
      w2.push(row);
    }
    const b2 = new Array(outputDim).fill(0.0);

    return {
      w1,
      b1,
      w2,
      b2,
      version: 1,
      trainedEpochs: 0,
      finalLoss: 0.28,
      updatedAt: Date.now(),
    };
  }

  private relu(x: number): number {
    return Math.max(0, x);
  }

  private sigmoid(x: number): number {
    const clamped = Math.max(-15, Math.min(15, x));
    return 1.0 / (1.0 + Math.exp(-clamped));
  }

  /**
   * Forward pass through the 2-layer network:
   * h = ReLU(W1 * x + b1)
   * y = Sigmoid(W2 * h + b2)
   */
  public forward(x: number[]): { y: number[]; h: number[] } {
    const h = new Array(this.hiddenDim).fill(0);
    for (let i = 0; i < this.hiddenDim; i++) {
      let sum = this.weights.b1[i];
      for (let j = 0; j < this.inputDim; j++) {
        sum += this.weights.w1[i][j] * x[j];
      }
      h[i] = this.relu(sum);
    }

    const y = new Array(this.outputDim).fill(0);
    for (let k = 0; k < this.outputDim; k++) {
      let sum = this.weights.b2[k];
      for (let i = 0; i < this.hiddenDim; i++) {
        sum += this.weights.w2[k][i] * h[i];
      }
      y[k] = this.sigmoid(sum);
    }

    return { y, h };
  }

  public computeLoss(dataset: TrainingSample[]): number {
    if (dataset.length === 0) return 0;
    let totalLoss = 0;
    for (const sample of dataset) {
      const { y } = this.forward(sample.inputs);
      for (let k = 0; k < this.outputDim; k++) {
        const diff = y[k] - sample.targets[k];
        totalLoss += diff * diff;
      }
    }
    return totalLoss / (dataset.length * this.outputDim);
  }

  /**
   * On-device training step with Momentum Gradient Descent
   */
  public async train(
    dataset: TrainingSample[],
    epochs = 30,
    learningRate = 0.04,
    momentum = 0.85,
    onProgress?: (p: TrainingProgress) => void
  ): Promise<{ finalLoss: number; lossHistory: number[]; weights: ModelWeights }> {
    const lossHistory: number[] = [];

    // Velocity accumulators for momentum
    const vW1: number[][] = this.weights.w1.map(row => new Array(row.length).fill(0));
    const vB1: number[] = new Array(this.weights.b1.length).fill(0);
    const vW2: number[][] = this.weights.w2.map(row => new Array(row.length).fill(0));
    const vB2: number[] = new Array(this.weights.b2.length).fill(0);

    for (let epoch = 1; epoch <= epochs; epoch++) {
      // Gradient accumulators
      const gradW1: number[][] = this.weights.w1.map(row => new Array(row.length).fill(0));
      const gradB1: number[] = new Array(this.weights.b1.length).fill(0);
      const gradW2: number[][] = this.weights.w2.map(row => new Array(row.length).fill(0));
      const gradB2: number[] = new Array(this.weights.b2.length).fill(0);

      let epochLoss = 0;

      for (const sample of dataset) {
        const { y, h } = this.forward(sample.inputs);

        // Output delta: dL/dz2 = (y - t) * y * (1 - y)
        const delta2 = new Array(this.outputDim).fill(0);
        for (let k = 0; k < this.outputDim; k++) {
          const diff = y[k] - sample.targets[k];
          epochLoss += diff * diff;
          delta2[k] = diff * y[k] * (1.0 - y[k]);
          gradB2[k] += delta2[k];
          for (let i = 0; i < this.hiddenDim; i++) {
            gradW2[k][i] += delta2[k] * h[i];
          }
        }

        // Hidden delta: dL/dz1 = (W2^T * delta2) * (h > 0 ? 1 : 0)
        const delta1 = new Array(this.hiddenDim).fill(0);
        for (let i = 0; i < this.hiddenDim; i++) {
          let sum = 0;
          for (let k = 0; k < this.outputDim; k++) {
            sum += this.weights.w2[k][i] * delta2[k];
          }
          delta1[i] = h[i] > 0 ? sum : 0;
          gradB1[i] += delta1[i];
          for (let j = 0; j < this.inputDim; j++) {
            gradW1[i][j] += delta1[i] * sample.inputs[j];
          }
        }
      }

      const n = Math.max(1, dataset.length);
      epochLoss /= n * this.outputDim;
      lossHistory.push(epochLoss);

      // Apply gradients with momentum
      for (let k = 0; k < this.outputDim; k++) {
        vB2[k] = momentum * vB2[k] + learningRate * (gradB2[k] / n);
        this.weights.b2[k] -= vB2[k];
        for (let i = 0; i < this.hiddenDim; i++) {
          vW2[k][i] = momentum * vW2[k][i] + learningRate * (gradW2[k][i] / n);
          this.weights.w2[k][i] -= vW2[k][i];
        }
      }

      for (let i = 0; i < this.hiddenDim; i++) {
        vB1[i] = momentum * vB1[i] + learningRate * (gradB1[i] / n);
        this.weights.b1[i] -= vB1[i];
        for (let j = 0; j < this.inputDim; j++) {
          vW1[i][j] = momentum * vW1[i][j] + learningRate * (gradW1[i][j] / n);
          this.weights.w1[i][j] -= vW1[i][j];
        }
      }

      if (onProgress && (epoch % 5 === 0 || epoch === epochs)) {
        onProgress({
          epoch,
          totalEpochs: epochs,
          loss: epochLoss,
          isComplete: epoch === epochs,
        });
        // Yield to event loop
        await new Promise(r => setTimeout(r, 8));
      }
    }

    const finalLoss = lossHistory[lossHistory.length - 1];
    this.weights = {
      ...this.weights,
      trainedEpochs: this.weights.trainedEpochs + epochs,
      finalLoss,
      updatedAt: Date.now(),
    };

    return {
      finalLoss,
      lossHistory,
      weights: this.weights,
    };
  }

  public static generateSyntheticDataset(sampleCount = 25): TrainingSample[] {
    const dataset: TrainingSample[] = [];
    for (let s = 0; s < sampleCount; s++) {
      // Raw 5 inputs: [logins (1..15), timeMinutes (2..45), quizScore (20..100), difficulty (1..3), category (1..6)]
      const logins = 1 + (s % 12);
      const time = 5 + (s * 2.3) % 40;
      const quizScore = 40 + (s * 3.7) % 60;
      const difficulty = 1 + (s % 3);
      const category = 1 + (s % 6);

      const normInputs = this.normalize([logins, time, quizScore, difficulty, category]);

      // Targets [Conscientiousness, Motivation, Understanding, Engagement]
      const c = Math.min(1, Math.max(0, 0.3 + (logins / 15) * 0.5 + (time > 15 ? 0.2 : 0)));
      const m = Math.min(1, Math.max(0, 0.4 + (quizScore / 100) * 0.4 + (difficulty / 3) * 0.2));
      const u = Math.min(1, Math.max(0, (quizScore / 100) * 0.85 + 0.1));
      const e = Math.min(1, Math.max(0, 0.35 + (time / 45) * 0.4 + (logins / 15) * 0.25));

      dataset.push({
        inputs: normInputs,
        targets: [c, m, u, e],
      });
    }
    return dataset;
  }
}
