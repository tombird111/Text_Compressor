To compress/decompress a file: 
1) Move it from Textfiles to this Code folder.
2) Change the string arguments in the main method in the TextCompressor.java
3) Compile and run the program

Unfortunately, there are two problems with the program.
The first is that it is only compatible with ASCII character files.
The second is that if the Huffman tree becomes too large, then it cannot encode certain bytes
This is because the binary code created from the tree traversal extends over 9 bits
This results in false values in place for less common characters.
This is due to the way the bytes are witten into the file.