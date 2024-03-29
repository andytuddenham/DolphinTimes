<div align=center>
  <img src="res/images/DolphinTimesLogo.png" width="75"/>
  <h1>Dolphin Times</h1>
</div>


## Install - Windows
- Install Java: [installer](https://download.oracle.com/java/18/latest/jdk-18_windows-x64_bin.exe), [download page](https://www.oracle.com/java/technologies/downloads/#jdk18-windows)
- Download the latest DolphinTimes release [here](https://github.com/andytuddenham/DolphinTimes/releases/latest/download/DolphinTimes.zip).
- Extract All and move the folder to `C:\Program Files\`
- Create a new shortcut on your desktop, pointing to `C:\Program Files\DolphinTimes\DolphinTimes.jar`
- To add the icon to the shortcut: Right click > properties > change icon (at the bottom of the shortcut tab), browse or paste in `C:\Program Files\DolphinTimes\icon.ico` to the bar and select the icon file.


## Debugging

### If no results are showing up

Make sure the results file are being created by Dolphin.
They are usually in `C:\CTSDolphin` and should look like [this](https://github.com/andytuddenham/DolphinTimes/tree/0.3d/test_data).
If they are being created in a different directory then change the `dolphin.results.path` property in `C:\Program Files\DolphinTimes\dolphintimes.properties` to match.
