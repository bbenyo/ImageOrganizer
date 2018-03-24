# Image OrganizerWORK IN PROGRESSVery incomplete currently, ignore this unless you're me.# BuildingBuild using Gradle, wrapper is not committed, build the wrapper using:gradle wrapper --gradle-version 4.5Alternatively, the dependencies are simple, so use whatever you want. It should be easy to convert to any build system.An executable jar will be committed when its closer to a releasable form.#Plugin architecture for organizing personal images and videos.  Overall goal is to filter a large set of images/videos into a "good" set and a extra or backup set (and a potential delete set).  The good set can then be copied to a separate directory/subdirectory and easily shared/copied/synced with phones, etc.  While the backup/extra set contains all the other images/videos, and can be more easily moved to archival storage.Plugins can be fully automated, tagging images as "good", or interactive, asking the user for input.  Plugins can also apply more tags, such as identifying people/places, or trying to rename files from generic "001.jpg" to some descriptive filename "person1-place.jpg". Plugins do not move or delete files, just apply tags or rename files.The main controller can then move "good" or "delete" tagged files to a separate subdirectory.  It will not actually delete any files, just move.  Deleting the files in the "delete" subdirectory must be a manual operation by a human, to ensure that you really want to delete.  The automation should only tag/rename/move files.# PluginsThe architecture will extract a set of features for each picture/image.  List of features will be included here. ## Example features### Date### Delta time since last picture### Size### Some meta color features (overall brightness, color histogram, etc)These features can be used by plugins directly in a rule-based scheme, or used as input to a machine learning algorithm.## Burst DetectorToddlers love to grab the phone and take 100 pictures of the top of their head.  This can be detected pretty easily by the delta time since last picture feature, and similarity in the meta color features.  ### Automatic mode: Tag the first in a burst as "good" the rest as "delete" or "backup" depending on parameter### Interactive mode: Let the user pick whether to delete all, pick one representative as "good".* Decide whether to write a simple rule or use a decision tree* Likely do examples of both to compare and as example plugins## SameEvent Filter20 pictures of the same event, same people/place, similar time.  Pick one/two as "good", and move the rest to backup.  Expect a larger difference in delta time than the burst detector, but still should be easy to detect with a similar rule/decision tree.  Needs to be interactive.## Identical Detectormd5 hash to notice indentical pictures, automatically remove duplicates.  This happens from syncing with an old phone, copy/paste errors, importing using different methods, etc. Check file size first for a match before trying md5 to verify. Tag one as good, duplicates as "delete".## Manual TaggerLet the user tag manually with a simple interface, pop up a grid of 9 pictures, with good, delete buttons, and an optional tag field.# Future## What can we do with video?## People TaggerCan we tell who is in the images and tag them?## Location TaggerCan we tell anything about the location?