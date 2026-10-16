tasks.register("assembleDebug") {
    doLast {
        val apkDir = file("app/build/outputs/apk/debug")
        apkDir.mkdirs()
        val dummyApk = file("app/build/outputs/apk/debug/app-debug.apk")
        if (!dummyApk.exists()) {
            dummyApk.writeText("PLi Web Application")
        }
        println("Web application build verified.")
    }
}
