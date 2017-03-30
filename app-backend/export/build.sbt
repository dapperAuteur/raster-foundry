name := "rf-export"

assemblyJarName in assembly := "rf-export.jar"

mainClass in (Compile, assembly) := Some("com.azavea.rf.export.Export")

javaOptions += "-Xmx2G"

fork in run := true

assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case n if n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA") => MergeStrategy.discard
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case _ => MergeStrategy.first
}
