package types;

public class BoolType extends Type {
  final private String type = "bool";
  private Boolean value;

  public BoolType() {
    this.value = true;
  }

  public BoolType(Boolean value) {
    this.value = value;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Boolean getValue() {
    return value;
  }
}
