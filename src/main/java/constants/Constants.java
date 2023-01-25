package constants;

import java.io.File;

public enum Constants {
    // TODO: remove it
    EXAMPLES_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/examples/";
        }
    },
    ASSETS_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/storage/assets/";
        }
    },
    CONTRACTS_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/storage/contracts/";
        }
    },
    CONTRACT_INSTANCES_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/storage/contract-instances/";
        }
    },
    PROPERTIES_PATH {
        public String toString() {
            File currentDirectory = new File(new File(".").getAbsolutePath());
            return currentDirectory + "/storage/properties/";
        }
    }
}
