public class WeightedChar{
	private Character assignedChar;
	private int charCount;

	public WeightedChar(Character characterToAdd, int charCountToAdd){
		this.assignedChar = characterToAdd;
		this.charCount = charCountToAdd;
	}
	
	public Character getAssignedChar(){return this.assignedChar;}
	public int getCharCount() {return this.charCount;}
	public void setCharCount(int newCharCount) {this.charCount = newCharCount;}
}