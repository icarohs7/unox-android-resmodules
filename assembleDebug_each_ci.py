from utils import assemble_debug_each

gradle_exec = "./gradlew"
gradle_command_flags = "-x lint -PrelativeResMod -Pci -Pcoverage --stacktrace"
assemble_debug_each(gradle_exec, gradle_command_flags)
