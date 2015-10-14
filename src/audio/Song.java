/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

import javax.sound.midi.*;

/**
 * 
 * @author Caitlin Scarberry
 */
public class Song {
	public int bpm;
	public double length;
	public int[] timeSignature = new int[] { 4, 4 };
	public int[] keyAdjustments;
	private Sequence mySeq;
	private interpretation.Page p;
	public ArrayList<interpretation.Symbol> symbolsToPlay;
	public ArrayList<Integer> symbolTimes;
	public gui.Main myFrame;
	public Synthesizer mySynthesizer;
	public ArrayList<Note> notes = new ArrayList<Note>();
	public int instrument = 0;

	public Song(interpretation.Page myPage) {
		bpm = 120;
		p = myPage;
		symbolsToPlay = new ArrayList<interpretation.Symbol>();
		makeMidiSequence();
	}

	public double lengthInSeconds() {
		return mySeq.getMicrosecondLength() / 1000000.0;
	}

	public void play() {
		try {
			mySynthesizer = MidiSystem.getSynthesizer();
			
			long elapsedSynthTime = mySynthesizer.getMicrosecondPosition();
			mySynthesizer.open();
			mySynthesizer.getChannels()[0].programChange(instrument);
			Receiver synthReceiver = mySynthesizer.getReceiver();
			Timer t = new Timer();
			int currentSymbol = 0;
			for (int i = 0; i < mySeq.getTracks()[0].size(); i++) {
				if (mySeq.getTracks()[0].get(i).getMessage().getStatus() == ShortMessage.NOTE_ON) {
					try{
						t.schedule(new gui.updatePlayingNote(myFrame, this,
								currentSymbol), Math.max(0, (mySeq.getTracks()[0]
								.get(i).getTick()
								* mySeq.getMicrosecondLength()
								/ mySeq.getTickLength() + elapsedSynthTime)
								/ 1000
								- mySynthesizer.getMicrosecondPosition() / 1000));
						currentSymbol++;
					}
					catch(Exception e){
						e.printStackTrace();
						System.exit(0);
					}
				}
				synthReceiver.send(
						mySeq.getTracks()[0].get(i).getMessage(),
						mySeq.getTracks()[0].get(i).getTick()
							* mySeq.getMicrosecondLength()
							/ mySeq.getTickLength() + elapsedSynthTime);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int[] semitoneAdjustments(String key) {
		int[] keyAdjustments = new int[7];
			switch (key) {
				case "G major":
												//C D  E  F  G  A  B
					keyAdjustments = new int[] { 0, 0, 0, 1, 0, 0, 0 };
					break;
				case "D major":
												//C D  E  F  G  A  B
					keyAdjustments = new int[] { 1, 0, 0, 1, 0, 0, 0 };
					break;
				case "A major":
												//C D  E  F  G  A   B
					keyAdjustments = new int[] { 1, 0, 0, 1, 1, 0, 0 };
					break;
				case "E major":
												//C D  E  F  G  A   B
					keyAdjustments = new int[] { 1, 1, 0, 1, 1, 0, 0 };
					break;
				case "B major":
												//C D  E  F  G  A  B
					keyAdjustments = new int[] { 1, 1, 0, 1, 1, 1, 0 };
					break;
				case "F sharp major":
												//C D  E  F  G  A  B
					keyAdjustments = new int[] { 1, 1, 1, 1, 1, 1, 0 };
					break;
				case "C sharp major":
												//C D  E  F  G  A  B
					keyAdjustments = new int[] { 1, 1, 1, 1, 1, 1, 1};
					break;
				case "F major":
												//C D  E  F  G  A   B
					keyAdjustments = new int[] { 0, 0, 0, 0, 0, 0, -1 };
					break;
				case "B flat major":
												//C D  E  F  G  A   B
					keyAdjustments = new int[] { 0, 0, -1, 0, 0, 0, -1 };
					break;
				case "E flat major":
												//C D   E  F  G   A   B
					keyAdjustments = new int[] { 0, 0, -1, 0, 0, -1, -1 };
				case "A flat major":
												//C  D   E  F  G  A   B
					keyAdjustments = new int[] { 0, -1, -1, 0, 0, -1, -1 };
					break;
				case "D flat major":
												//C  D   E  F  G  A   B
					keyAdjustments = new int[] { 0, -1, -1, 0, -1, -1, -1 };
					break;
				case "G flat major":
												//C  D   E  F  G  A   B
					keyAdjustments = new int[] { -1, -1, -1, 0, -1, -1, -1 };
					break;
				case "C flat major":
												//C  D   E  F  G  A   B
					keyAdjustments = new int[] { -1, -1, -1, -1, -1, -1, -1 };
					break;
			}
			return keyAdjustments;
	}

	public int noteToSemitones(interpretation.MeasureRow mRow, interpretation.Symbol note, 
		interpretation.Symbol accidental) {
		boolean natural = false; 
		int halfTonesAboveMidC = 0;
		int staffDistFromMidC = 0;
		if (note.noteHeads.size() > 0)
			staffDistFromMidC = 0 - mRow.evalPitch(note.noteHeads.get(0)[1] + note.staffY);
		if (accidental != null) {
			if (note.noteHeads.size() > 0
					&& Math.abs(accidental.staffY
							+ (accidental.myImage.getHeight() / 2)
							- note.staffY - note.noteHeads.get(0)[1]) < note.staffDist * 1
					&& Math.abs(accidental.staffX - note.staffX) < note.staffDist * 1.5) {
				if (accidental.type.equals("Flat"))
					halfTonesAboveMidC--;
				else if (accidental.type.equals("Sharp"))
					halfTonesAboveMidC++;
				else
					natural = true;
			}
		}
		int scalePos = 0;
		int noteLine = 0;
		if(staffDistFromMidC>0) {
			//walk up the scale from middle c,
			//incrementing the semitones of the note as we go
			while (noteLine < staffDistFromMidC) {
				if (scalePos == 2 || scalePos == 6) {
					halfTonesAboveMidC++;
				} else {
					halfTonesAboveMidC += 2;
				}
				scalePos++;
				noteLine++;
				if (scalePos > 6)
					scalePos = 0;
			}
		}
		else {
			//walk down the scale from middle c,
			//decrementing the semitones of the note as we go
			while (noteLine > staffDistFromMidC) {
				if (scalePos == 3 || scalePos == 0)
					halfTonesAboveMidC--;
				else
					halfTonesAboveMidC -= 2;
				noteLine--;
				scalePos--;
				if (scalePos < 0)
					scalePos = 6;
			}
		}

		if(!natural)
			halfTonesAboveMidC += keyAdjustments[scalePos % 7];
		return halfTonesAboveMidC;
	}

	public int nameToLength(interpretation.Symbol s, int originalLenth) {
		int noteLength = originalLenth;
		if (s.type.contains("Half"))
			noteLength /= 2;
		else if (s.type.contains("Quarter"))
			noteLength /= 4;
		else if (s.type.contains("Eighth"))
			noteLength /= 8;
		else if (s.type.contains("Sixteenth"))
			noteLength /= 16;
		return noteLength;
	}

	public void makeNoteSequence() {

		try {
			notes = new ArrayList<Note>();			
			symbolsToPlay = new ArrayList<interpretation.Symbol>();
			symbolTimes = new ArrayList<Integer>();
			boolean tie = false;
			int ticksPassed = 0;

			if (p.measureRows.get(0).key == null)
				p.measureRows.get(0).key = "C major";
			keyAdjustments = semitoneAdjustments(p.measureRows.get(0).key);

			interpretation.Symbol accidental = null;
			int[] repeatStart = new int[] {-1, 0};

			for (int measureRowCount = 0; measureRowCount < p.measureRows
					.size(); measureRowCount++) {
				interpretation.MeasureRow mRow = p.measureRows
						.get(measureRowCount);
				for (int symbolCount = 0; symbolCount < mRow.symbols.size();
					symbolCount++) {
					interpretation.Symbol s = mRow.symbols.get(symbolCount);
					
					if (s.type.equals("Repeat start"))
						repeatStart = new int[] {symbolCount, measureRowCount};

					else if (s.type.equals("Repeat end") && repeatStart[0] > 0) {
						measureRowCount = repeatStart[1];
						mRow = p.measureRows.get(measureRowCount);
						symbolCount = repeatStart[0];
						repeatStart = new int[] {-1, 0};
					}
					
					else if (s.type.contains("note") && s.noteHeads.size() > 0) {
						symbolsToPlay.add(s);

						int noteLength = nameToLength(s,1024);
						noteLength = (int) (noteLength *  timeSignature[0]);

						if (symbolCount < mRow.symbols.size() - 1
								&& mRow.symbols.get(symbolCount + 1).type
										.equals("Dot"))
							noteLength *= 1.5;						

						int halfTonesAboveMidC = noteToSemitones(mRow,s,accidental);

						tie = false;
						if(symbolCount > 1 && (mRow.symbols.get(symbolCount-1).type.equals("Tie")||mRow.symbols.get(symbolCount-2).type.equals("Tie"))){
							if(!mRow.symbols.get(symbolCount-1).type.contains("note"))
								tie = true;
						}
						if(symbolCount > 2 && (mRow.symbols.get(symbolCount-3).type.equals("Tie"))){
							if(!mRow.symbols.get(symbolCount-2).type.contains("note"))
								if(!mRow.symbols.get(symbolCount-1).type.contains("note"))
									tie = true;
						}
						if(notes.size()>0)
							notes.get(notes.size()-1).slurred = tie;

						symbolTimes.add(new Integer(ticksPassed));
						ticksPassed += noteLength;
						notes.add(new Note(s, halfTonesAboveMidC, noteLength));
					} 

					else if (s.type.contains("rest")) {
						int restLength = (int)(nameToLength(s, 1024) * timeSignature[0]);
						ticksPassed += restLength;
						notes.add(new Note(s, 0, restLength));
						if(notes.size()>0)
							notes.get(notes.size()-1).volume = 0;
					}

					if (s.type.equals("Flat") || s.type.equals("Sharp") || s.type.equals("Natural"))
						accidental = s;
					else
						accidental = null;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void makeMidiSequence() {
		try {
			mySeq = new Sequence(Sequence.PPQ, 512 * bpm / 60);
			symbolsToPlay = new ArrayList<interpretation.Symbol>();
			symbolTimes = new ArrayList<Integer>();
			
			Track t = mySeq.createTrack();

			t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE,
					instrument, 80), 0));
			int ticksPassed = 0;
			
			for(int noteCount = 0; noteCount<notes.size(); noteCount++){
				Note n = notes.get(noteCount);
				ShortMessage myMsg = new ShortMessage();
				myMsg.setMessage(ShortMessage.NOTE_ON, 0,
				48 + n.semiTonesAboveMidC, n.volume);
				
				if(n.slurred&&noteCount<notes.size() &&
					notes.get(noteCount-1).semiTonesAboveMidC==n.semiTonesAboveMidC) {
					myMsg.setMessage(ShortMessage.NOTE_ON, 0,
							48, 10);		
				}
				MidiEvent event = new MidiEvent(myMsg, ticksPassed);
				t.add(event);
				
				ticksPassed += n.tickLength;
				myMsg = new ShortMessage();
				myMsg.setMessage(ShortMessage.NOTE_OFF, 0,
						48 + n.semiTonesAboveMidC, 0);
				if(noteCount+1<notes.size() && n.slurred &&
					notes.get(noteCount+1).semiTonesAboveMidC==n.semiTonesAboveMidC) {
					ticksPassed+=notes.get(noteCount+1).tickLength;
					noteCount++;
				}
				else
					t.add(new MidiEvent(myMsg,ticksPassed));							
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
