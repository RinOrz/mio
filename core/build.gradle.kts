//androidLib {
//  sourceSets.main.manifest.srcFile("src/androidMain/AndroidManifest.xml")
//}

commonTarget {
  main.dependencies {
    apiOf(
      "com.meowool.toolkit:sweekt:_",
      Libs.KotlinX.Coroutines.Core,
    )
  }
  test.dependencies {
    implementationOf(Libs.Kotest.Runner.Junit5.Jvm)
  }
}

//androidTarget {
//  main.dependencies {
//    api(Libs.AndroidX.Core.Ktx)
//    compileOnlyOf(
//      Libs.AndroidX.Lifecycle.ViewModel,
//      Libs.AndroidX.Lifecycle.Livedata,
//      Libs.AndroidX.Activity.Ktx,
//      Libs.AndroidX.Fragment.Ktx,
//    )
//  }
//}

jvmTarget {
  configureTestRunTask {
    useJUnitPlatform()
  }
  main.dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:_")
  }
}
