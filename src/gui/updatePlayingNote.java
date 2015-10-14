package gui;

import java.util.TimerTask;

public class updatePlayingNote extends TimerTask {
	private Main myFrame;
	private audio.Song mySong;
	private int currentSymbol;
	public updatePlayingNote(Main myFrame, audio.Song mySong, int currentSymbol){
		super();
		this.myFrame = myFrame;
		this.mySong = mySong;
		this.currentSymbol = currentSymbol;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(currentSymbol>0&&currentSymbol<mySong.notes.size()){
			mySong.notes.get(currentSymbol-1).mySymbol.playing = false;
			
			mySong.notes.get(currentSymbol).mySymbol.playing = true;
		}
		myFrame.drawSymbolBoxes();
	}

}
