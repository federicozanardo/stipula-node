package trap;

public enum TrapErrorCodes {
  ERROR_CODE_DOES_NOT_EXISTS {
    public String toString() {
      return "ERROR_CODE_DOES_NOT_EXISTS";
    }
  },
  INCORRECT_TYPE {
    public String toString() {
      return "INCORRECT_TYPE";
    }
  },
  INSTRUCTION_DOES_NOT_EXISTS {
    public String toString() {
      return "INSTRUCTION_DOES_NOT_EXISTS";
    }
  },
  LABEL_DOES_NOT_EXISTS {
    public String toString() {
      return "LABEL_DOES_NOT_EXISTS";
    }
  },
  MISS_HALT_INSTRUCTION {
    public String toString() {
      return "MISS_HALT_INSTRUCTION";
    }
  },
  NOT_ENOUGH_ARGUMENTS {
    public String toString() {
      return "NOT_ENOUGH_ARGUMENTS";
    }
  },
  QUEUE_OVERFLOW {
    public String toString() {
      return "QUEUE_OVERFLOW";
    }
  },
  QUEUE_UNDERFLOW {
    public String toString() {
      return "QUEUE_UNDERFLOW";
    }
  },
  STACK_OVERFLOW {
    public String toString() {
      return "STACK_OVERFLOW";
    }
  },
  STACK_UNDERFLOW {
    public String toString() {
      return "STACK_UNDERFLOW";
    }
  },
  TYPE_DOES_NOT_EXIST {
    public String toString() {
      return "TYPE_DOES_NOT_EXIST";
    }
  },
  VARIABLE_ALREADY_EXIST {
    public String toString() {
      return "VARIABLE_ALREADY_EXIST";
    }
  },
  VARIABLE_DOES_NOT_EXIST {
    public String toString() {
      return "VARIABLE_DOES_NOT_EXIST";
    }
  }
}
