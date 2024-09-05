import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class TextCompressor{
	private HashMap<Character, Integer> presentCharacters = new HashMap<Character, Integer>();
	private WeightedChar[] weightedCharArray;
	private HashSet<LeafNode> leafNodes = new HashSet<LeafNode>();
	private HashSet<Node> binaryTree = new HashSet<Node>();
	private String textToCompress;
	
	public static void main(String[] args){
		TextCompressor compressor = new TextCompressor();
		compressor.compressFile("Frankenstein.txt");
		System.out.println("--- DECODING ---");
		compressor.decompressFile("Frankenstein.txt");
	}
	
	public void decompressFile(String fileName){ //Decompresses a file with filename
		presentCharacters.clear(); //Clear any presentCharacters
		assignCharactersFromText(fileName); //Create presentCharacters from the characters text file
		createWeightedCharArray();
		sortArray();
		growTree(); //Make the tree
		byte[] byteArray = readBinFile(fileName); //Get the bytes from the .bin file
		String decompressedText = byteArrayToString(byteArray); //Create the string
		writeTextToFile(decompressedText, fileName); //Write the string to a new file
	}
	
	public void writeTextToFile(String decompressedText, String fileName){ //Write a decompressed file to a text file
		File outputFile = new File("Decompressed" + fileName + ".txt"); //Create a new file
		BufferedWriter outputWriter = null; //Create a null output writer
		try{
			outputWriter = new BufferedWriter(new FileWriter(outputFile)); //Set the buffered writer output to the output file 
			outputWriter.write(decompressedText); //Write the info into the file
		} catch (IOException ex){
			ex.printStackTrace(); //Print the stack trace if anything goes wrong
		} finally {
			try{
				if(outputWriter != null) outputWriter.close(); //If the stream is not null, close it
			} catch(IOException ex) {
				System.out.println("Failed to close files");
			}
		}
	}
	
	public String byteArrayToString(byte[] byteArray){ //Convert a byte array to a string
		String decompressedText = "";
		for(byte currentByte : byteArray){ //For each byte in the byteArray
			decompressedText += byteToChar(currentByte); //Convert it to a character
		}
		return decompressedText; //Return the string
	}
	
	public Character byteToChar(byte currentByte){ //Convert a byte to a character
		Character character = findLeafInHashSet(currentByte).getWeightedChar().getAssignedChar(); //Find the LeafNode containing the Byte, 
		return character;
	}
	
	public byte[] readBinFile(String fileName){ //Read a binary file
		FileInputStream fileInputStream = null;
		try {
			File inputFile = new File("Compressed" + fileName + ".bin");
			byte[] byteArray = new byte[(int)inputFile.length()];
			fileInputStream = new FileInputStream(inputFile);
			fileInputStream.read(byteArray);
			fileInputStream.close();
			return byteArray;
		} catch (IOException error) { //Throw an exception if required
			System.out.println(error.toString());
			error.printStackTrace();
			return null;
		}
		
	}
	
	public void assignCharactersFromText(String fileName){ //Assign characters from a file with filename to presentCharacters
		String characterFileName = "Characters" + fileName + ".txt"; //Create the name of the file you wish to make
		String fileString = readTxtFromFile(characterFileName); //Get the file text using the file name
		boolean recordingCharacter = true; //Record the first character
		Character characterToAdd = ' '; //Create an empty character to add
		String countString = ""; //Create a string that will be used to contain the count
		for(int index = 0; index < fileString.length(); index++){
			Character currentCharacter = fileString.charAt(index);
			if(recordingCharacter){ //If you are currently recording the character
				characterToAdd = currentCharacter; //Note the character to add
				recordingCharacter = false; //Do not record anymore characters
				index++; //Skip the first |
				countString = ""; //Empty the count string
			} else if (currentCharacter == '|') { //If you have reached the end of the numbers
				presentCharacters.put(characterToAdd, Integer.parseInt(countString)); //Add the character and its respective count to the presentCharacters HashMap
				recordingCharacter = true; //Note to record the next character
			} else { //If you have got a characterToAdd, but have not reached a | yet
				countString += currentCharacter; //Add the current character to the count string
			}
		}
	}
	
	public void compressFile(String fileName){ //Compress a file with text fileName
		textToCompress = readTxtFromFile(fileName); //Get the text from string
		assignCharactersToHashMap(textToCompress); //Assign all characters from the string to PresentCharacters
		createWeightedCharArray();
		sortArray(); //Sort the array
		growTree(); //Construct a tree based on the weightedCharArray
		encodeFile(textToCompress, fileName);
	}
	
	public void encodeFile(String textToCompress, String fileName){ //Encode a file with the text to compress, and the filename
		byte[] byteArray = new byte[textToCompress.length()];
		for(int index = 0; index < textToCompress.length(); index++){ //For every character in textToCompress
			byteArray[index] = findLeafInHashSet(textToCompress.charAt(index)).getCodedByte(); //Put the characters corresponding codedByte into the array
		}
		File outputFile = new File("Compressed" + fileName + ".bin"); //Create a new file
		FileOutputStream outputStream = null; //Create a null output stream
		try{
			outputStream = new FileOutputStream(outputFile); //Set the stream to output to the outputFile 
			for(byte currentByte : byteArray){
				outputStream.write(currentByte); //Place every currentByte into the file
			}
		} catch (IOException ex){
			ex.printStackTrace(); //Print the stack trace if anything goes wrong
		} finally {
			try{
				if(outputStream != null) outputStream.close(); //If the stream is not null, close it
			} catch(IOException ex) {
				System.out.println("Failed to close files");
			}
		}
		makePresentCharactersFile(fileName);
	}
	
	public void makePresentCharactersFile(String fileName){ //Create a file based on the PresentCharacters
		File outputFile = new File("Characters" + fileName + ".txt"); //Create a new file
		BufferedWriter outputWriter = null; //Create a null output writer
		try{
			outputWriter = new BufferedWriter(new FileWriter(outputFile)); //Set the buffered writer output to the output file 
			for(Character currentChar : presentCharacters.keySet()){ //For each character in the presentCharacters HashMap
				String characterInfo = currentChar + "|" + presentCharacters.get(currentChar) + "|"; //Create info in the format Character|count|
				outputWriter.write(characterInfo); //Write the info into the file
			}
		} catch (IOException ex){
			ex.printStackTrace(); //Print the stack trace if anything goes wrong
		} finally {
			try{
				if(outputWriter != null) outputWriter.close(); //If the stream is not null, close it
			} catch(IOException ex) {
				System.out.println("Failed to close files");
			}
		}
	}
	
	public void growTree(){ //Make a binary tree based on the WeightedChars
		ArrayList<Node> weightedNodeList = new ArrayList<Node>();
		for(WeightedChar wCharToAdd : weightedCharArray){
			LeafNode newNode = new LeafNode(wCharToAdd); //Create a LeafNode for every WeightedChar
			leafNodes.add(newNode); //Add the LeafNode to a list of LeafNodes
			weightedNodeList.add(newNode); //Add all the nodes to the binaryTree
		}
		while(weightedNodeList.size() > 1){ //Whilst the weightedNodeList is not empty
			Node nodeX = getLowestNode(weightedNodeList); //Find the lowest node
			Node nodeY = getLowestNode(weightedNodeList); //Find the second lowest node
			Node nodeZ = new Node(nodeX, nodeY); //Create a new Node with the children X and Y
			nodeX.setParent(nodeZ); //Set the parent of X and Y to be node Z
			nodeY.setParent(nodeZ);
			weightedNodeList.add(nodeZ); //Add nodeZ to the list
			binaryTree.add(nodeZ); //Add nodeZ to the binary tree
		}
		for(LeafNode nodeInTree : leafNodes){
			nodeInTree.encodeCharacter(); //Encode all leaf nodes
			//System.out.println(nodeInTree.getCodedByte() + " with character" + nodeInTree.getWeightedChar().getAssignedChar());
		}
	}
	
	public Node getLowestNode(ArrayList<Node> weightedNodeList){ //Get the lowest node the weightedNodeList
		Node lowestNode = weightedNodeList.get(0); //Set the first node as the lowest Node
		int index = 0;
		int lowestIndex = 0;
		for(Node currentNode : weightedNodeList){ //For every node in the list
			if(lowestNode.getTotalCount() > currentNode.getTotalCount()){ //Check if it is smaller than the current smallest node
				lowestNode = currentNode; //If it is, replace it
				lowestIndex = index; //Mark the index of the lowest node
			}
			index++;
		}
		weightedNodeList.remove(lowestIndex);
		return lowestNode;
	}
	
	public void sortArray(){ //Sort the weightedCharArray
		int first = 0; //From the first element in the array
		int last = weightedCharArray.length - 1; //To the last element of the array
		mergeSortArray(weightedCharArray, first, last); //Perform a mergesort on the whole array
	}
	
	public void assignCharactersToHashMap(String textToCompress){ //Assign characters to the presentCharacters hashmap 
		presentCharacters.clear();
		for(int position = 0; position < textToCompress.length(); position++){ //For the length of the text
			Character currentCharacter = textToCompress.charAt(position); //Get the current character from the text
			if(presentCharacters.keySet().contains(currentCharacter)){ //If the character can be found in the presentCharacters HashMap
				presentCharacters.put(currentCharacter, presentCharacters.get(currentCharacter) + 1); //Increase the count of the character in the HashMap
			} else {
				presentCharacters.put(currentCharacter, 1); //Add the character to a HashMap of present characters with the value of 1
			}
		}
	}
	
	public void createWeightedCharArray(){
		weightedCharArray = new WeightedChar[presentCharacters.size()]; //Set the size of WeightedCharArray to the number of present characters
		int index = 0;
		for (Character currentCharacter : presentCharacters.keySet()){ //For each character found
			WeightedChar newChar = new WeightedChar(currentCharacter, presentCharacters.get(currentCharacter)); //Create a new weighted character with the corresponding character and count
			weightedCharArray[index] = newChar; //Assign each new character to the array
			index++; //Iterate along the array by 1
		}
	}
	
	public void mergeSortArray(WeightedChar[] weightedCharArray, int first, int last){ //Merge sort the WeightedCharArray
		if(first < last){ //If the array is not yet sorted
			int midpoint = (first + last)/2; //Calculate the midpoint
			mergeSortArray(weightedCharArray, first, midpoint); //Sort from the first point to the midpoint
			mergeSortArray(weightedCharArray, midpoint + 1, last); //Sort the remainder of the array
			merge(weightedCharArray, first, midpoint, last); //Merge the array
		}
	}
	
	public void merge(WeightedChar[] weightedCharArray, int first, int midpoint, int last){ //Merge two arrays in the WeightedCharArray
		int array1Index, array2Index; //Note two indexes to use
		WeightedChar[] tempArray = new WeightedChar[weightedCharArray.length]; //Create a new array to store values
		for(array1Index = midpoint + 1; array1Index > first; array1Index--){ //Copy the lower array into the temporary array
			tempArray[array1Index - 1] = weightedCharArray[array1Index-1];
		}
		for(array2Index = midpoint; array2Index < last; array2Index++){ //Copy the upper array into the temporary array
			 tempArray[array2Index + 1] = weightedCharArray[last + midpoint - array2Index];
		}
		for(int index = first; index <= last; index++){ //For every member of the array
			if(tempArray[array2Index].getCharCount() < tempArray[array1Index].getCharCount()){ //If the character at index2 has a lower count than index1
				weightedCharArray[index] = tempArray[array2Index--]; //Move the relevant value into its respective position in the weightedCharArray
			} else {
				weightedCharArray[index] = tempArray[array1Index++]; //Move the relevant value into its respective position in the weightedCharArray
			}
		}
	}
	
	public LeafNode findLeafInHashSet(Character charToFind){ //Find a leaf node in the hashset that contains the character to find
		for(LeafNode currentLeafNode : leafNodes){ //For every leaf in the HashSet
			if(currentLeafNode.getWeightedChar().getAssignedChar().equals(charToFind)){ //If the character related to the LeafNode is the same as charToFind, return it
				return currentLeafNode;
			}
		}
		return null;
	}
	
	public LeafNode findLeafInHashSet(byte byteToFind){ //Find a leaf node in the hashset that contains the byte to find
		for(LeafNode currentLeafNode : leafNodes){ //For every leaf in the HashSet
			if(currentLeafNode.getCodedByte() == byteToFind){ //If the byte related to the LeafNode is the same as byteToFind, return it
				return currentLeafNode;
			}
		}
		return null;
	}
	
	public static String readTxtFromFile(String fileName){ //Read text from a file
		String fileText = ""; //Create an empty string
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader); //Create a new file reader
			String line = "";
			while(line != null){
				line = bufferedReader.readLine(); //Read every line
				if(line != null){ //Do not add any null lines
					fileText += line + "\n"; //Add a new line to the end of every read line
				}
			}
			bufferedReader.close();// Always close it.
		} catch (IOException error) { //Throw an exception if required
				System.out.println(error.toString());
				error.printStackTrace();
		}
		return fileText;
	}
}