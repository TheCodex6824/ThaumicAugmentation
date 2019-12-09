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
2. Run "./gradlew setupdecompworkspace" (*nix) or "gradlew setupdecompworkspace" (Windows)
3. For Eclipse users:
       &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Run "gradlew eclipse"  
   For IntelliJ IDEA:
       &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Run "gradlew idea"  
   
   Note that IDE users will probably need to add the following JVM argument to their IDE's launch options to allow the coremod to work (this only has to be done if working on the mod itself, not if just using the API):
   
   -Dfml.coreMods.load=thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore

At the end of this, all the dependencies and such should be set up automatically, as they are downloaded from CurseForge using a Gradle plugin. If weird HTTP errors come back when trying to download them, try updating the "CurseMaven" plugin in build.gradle (and then come yell at me to keep the plugin up-to-date).

[issues]: https://github.com/TheCodex6824/ThaumicAugmentation/issues
[lang]: https://github.com/TheCodex6824/ThaumicAugmentation/tree/master/src/main/resources/assets/thaumicaugmentation/lang