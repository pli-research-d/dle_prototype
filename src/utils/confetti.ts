import confetti from 'canvas-confetti';

export const triggerBadgeConfetti = () => {
  confetti({
    particleCount: 80,
    spread: 70,
    origin: { y: 0.6 },
    colors: ['#06B6D4', '#6366F1', '#10B981', '#F59E0B', '#EC4899'],
  });
};

export const triggerLevelUpConfetti = () => {
  const duration = 2000;
  const animationEnd = Date.now() + duration;

  const frame = () => {
    confetti({
      particleCount: 5,
      angle: 60,
      spread: 55,
      origin: { x: 0 },
      colors: ['#06B6D4', '#38BDF8', '#818CF8'],
    });
    confetti({
      particleCount: 5,
      angle: 120,
      spread: 55,
      origin: { x: 1 },
      colors: ['#10B981', '#F59E0B', '#EC4899'],
    });

    if (Date.now() < animationEnd) {
      requestAnimationFrame(frame);
    }
  };
  frame();
};
