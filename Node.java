import java.util.ArrayList;
import java.lang.Math;

public class Node{
	protected int totalCount;
	protected Node parent;
	private Node child0;
	private Node child1;
	private boolean rootNode;
	
	public Node(Node node0, Node node1){
		this.child0 = node0;
		this.child1 = node1;
		this.totalCount = (node0.getTotalCount() + node1.getTotalCount());
		this.rootNode = false;
	}
	
	public Node(){this.rootNode = false;}

	public boolean hasParent(){
		if(this.parent != null){
			return true;
		} else {
			return false;
		}
	}
	
	public Node getChildFromInt(int binNum){
		if(binNum == 1){
			return this.child1;
		} else {
			return this.child0;
		}
	}
	
	public boolean isRoot(){return this.rootNode;}
	public int getTotalCount(){return this.totalCount;}	
	public Node getParent(){return this.parent;}
	public void setParent(Node parentNode){this.parent = parentNode;}
	public void setRootNode(){this.rootNode = true;}
}

class LeafNode extends Node{
	private WeightedChar matchingWeightedChar;
	private byte codedByte;
	
	public LeafNode(WeightedChar newChar){
		this.matchingWeightedChar = newChar;
		this.totalCount = newChar.getCharCount();
	}
	
	public WeightedChar getWeightedChar(){return this.matchingWeightedChar;}
	public byte getCodedByte(){return this.codedByte;}
	
	public void encodeCharacter(){
		int[] binArray = new int[getNodeDepth()];
		int bIndex = 0;
		Node currentNode = this; //Starting with this node
		while(currentNode != null){ //Whilst the node you are looking at is not null
			if(currentNode.getParent() != null){ //Check if the node has a parent
				if(currentNode.getParent().getChildFromInt(0).equals(currentNode)){ //See if the current node is child0 of its parent
					binArray[bIndex] = 0; //If it is, add 0 to the array
				} else {
					binArray[bIndex] = 1; //if it isn't, add 1 to the array
				}
			}
			bIndex++;
			currentNode = currentNode.getParent(); //Set this node as the current node
		}
		String binString = "";
		for(int binNum : binArray){
			binString += binNum;
		}
		this.codedByte = calculateByteTotal(binArray);
	}
	
	private byte calculateByteTotal(int[] binArray){
		byte sumTotal = 0; //Summing the total of all values
		int binIndices = 0; //Beginning with an indices of 2
		for(int index = binArray.length - 1; index >= 0; index--){ //For every number in the binArray
			sumTotal += (binArray[index]*(Math.pow(2, binIndices))); //Add (0 or 1 * 2 to the power of binIndices)
			binIndices++; //Increase binIndices by 1
		}
		return sumTotal;
	}
	
	private int getNodeDepth(){
		int count = 0;
		Node nodey = this; //Starting with this node
		while(nodey != null){ //Whilst the node you are looking at is not null
			if(nodey.getParent() != null){ //Check if the node has a parent
				count++; //If it does, then add 1 to the count
			}
			nodey = nodey.getParent(); //Set this node as the current node
		}
		return count;
	}
}