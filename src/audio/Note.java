package audio;

public class Note {
	public interpretation.Symbol mySymbol;
	public int semiTonesAboveMidC;
	public int tickLength;
	public int volume;
	public boolean slurred;
	
	public Note(interpretation.Symbol s, int semiTonesAboveMidC, int length){
		mySymbol = s;
		volume = 90;
		this.semiTonesAboveMidC = semiTonesAboveMidC;
		this.tickLength = length;
	}
}
