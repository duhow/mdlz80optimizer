/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.microprocessor;

import java.util.ArrayList;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class PlainZ80Memory implements IMemory {    
    public static final int MEMORY_SIZE = 0x10000;
    public final int[] memory;
    public List<Pair<Integer, Integer>> writeProtections = new ArrayList<>();

    public PlainZ80Memory() {
        this.memory = new int[MEMORY_SIZE];
    }

    @Override
    final public int readByte(int address) {
        return memory[address];
    }

    @Override
    final public int readByteUntracked(int address) {
        return memory[address];
    }

    @Override
    final public int readWord(int address) {
        return readByte(address) + readByte((address + 1) & 0xffff) * 256;
    }

    @Override
    final public void writeByte(int address, int data) 
    {
        for(Pair<Integer, Integer> p:writeProtections) {
            if (address >= p.getLeft() && address < p.getRight()) return;
        }
        memory[address] = data;
    }

    @Override
    final public void writeWord(int address, int data) {
        writeByte(address, (data & 0x00ff));
        address = (address + 1) & 0xffff;
        data = (data >>> 8);
        writeByte(address, data);
    }
    
    
    @Override
    final public void writeProtect(int start, int end)
    {
        writeProtections.add(Pair.of(start, end));
    }
    
    
    @Override
    final public void clearWriteProtections()
    {
        writeProtections.clear();
    }    
    
    
    @Override
    final public int[] getMemoryArray()
    {
        return memory;
    }
}