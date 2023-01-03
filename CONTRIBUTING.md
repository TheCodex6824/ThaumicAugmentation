Contributing
-----
Contributions of pretty much any type are welcome! This includes people that are new to contributing to open source projects - we all have to start somewhere, and I'm completely fine with helping people out.  

1. **Bugfixes**  
    If you find a bug, and it is not already in the [issues section][issues], please report it there. If you feel confident that you can fix it yourself, then feel free to make a pull request. If it looks good then it will probably be merged fairly quickly, otherwise I will be happy to let you know what needs to be changed to make it perfect.  
2. **Features**  
    Features should probably involve a bit more discussion than bug fixes before you consider working on it - making an issue for it is fine. 
3. **Translations**  
    Translations would be very much welcome, as the only language I feel confident enough to localize for is English. The translation files can be found under [this folder][lang]. Even if a translation already exists, looking over what's already there would help. Try to name the file with the correct region code (e.g. en_us for US English). If you aren't sure that's fine, I can handle it for you. Lastly, I do recognize that there is quite a lot of text in the lang files - especially when taking the research into account. Even if you only can/want to localize a subset of the lang file, it would still be greatly appreciated.
4. **Textures and Models**  
    Changes to textures and models are welcome as long as they are *improvements* to them, and fit together with the rest of the mod. If they are *alternate* textures, they may be better off in a resource pack.
    
Setting Up a Dev Environment
-----
If you want to contribute to the mod or just mess around with it, here's how to do that:  
1. Clone the repository and change to that directory
2. To just build a jar file of the mod, run "./gradlew build" (*nix) or "gradlew build" (Windows)
3. Or, continue by opening the project in the IDE of your choice (but see below first)

The next section documents a (hopefully) working method of preparing the mod for development in an IDE. Unfortunately, the toolchain and environment changes over the years have made the 1.12 build system, especially its IDE support, quite brittle. The following instructions should provide a starting point, but may break in the future as things continue changing.

IDE Setup
-----
Firstly, I suggest ignoring all of the existing Gradle tasks for generating IDE project files. While they may work for simpler projects, Thaumic Augmentation uses enough of the advanced features of Forge and Gradle that the generated project is often wrong, especially when it comes to dependency management and ensuring the game can actually launch.
Instead, open your IDE of choice and import Thaumic Augmentation as a Gradle project. You will need the Gradle plugin if you are using IDEA, and the Buildship plugin for Eclipse. I believe there is also a Gradle plugin for vscode, but I have never personally used it. After importing the project, everything should be working in the IDE at this point. To run the game, you can use the `runClient` or `runServer` Gradle tasks.

If you decide to not use Gradle plugins and instead generate the project files, you *may* succeed, but I found that Eclipse in particular does not work very well.

[issues]: https://github.com/TheCodex6824/ThaumicAugmentation/issues
[lang]: https://github.com/TheCodex6824/ThaumicAugmentation/tree/master/src/main/resources/assets/thaumicaugmentation/lang
[build.gradle]: https://github.com/TheCodex6824/ThaumicAugmentation/blob/master/build.gradle
