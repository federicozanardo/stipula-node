package constants;

import java.io.File;

public enum Constants {
    DEFAULT_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory.getPath();
        }
    },
    EXAMPLES_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/examples/";
        }
    },

    STORAGE_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/storage/";
        }
    },
}
