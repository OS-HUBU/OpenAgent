package rapastcity3.Strategy;
/**
 * agent状态上下文
 * @author hzw
 *
 */
public class StatusContext {
 private String type;
 private StatusStrategy strategy;
 
 public StatusContext(String type,StatusStrategy strategy)
 {
	 this.type=type;
	 this.strategy=strategy;
 }

public String getType() {
	return type;
}

public void setType(String type) {
	this.type = type;
}

public StatusStrategy getStrategy() {
	return strategy;
}

public void setStrategy(StatusStrategy strategy) {
	this.strategy = strategy;
}
 public boolean options(String type)
 {
	 return this.type.equals(type);
 }
}
