set INPUT=C:/build/workspaces/riena2/org.eclipse.riena.build3/prebuild
set OUTPUT=file:/C:/build/workspaces/riena2/org.eclipse.riena.build3/prebuild-out

java -jar C:\build\develop\eclipse-3.7/plugins/org.eclipse.equinox.launcher_*.jar ^
-application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher ^
-metadataRepository %OUTPUT% ^
-artifactRepository %OUTPUT% ^
-source %INPUT% ^
-configs win32.win32.x86 -compress -publishArtifacts