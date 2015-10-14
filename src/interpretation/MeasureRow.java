package interpretation;


import java.io.IOException;
import java.util.ArrayList;

public class MeasureRow {
	public MusicImage originalPage;
	MusicImage toTakeSymbolsFrom;
	public int[][] staffLines;
	int staffStartX;
	int staffEndX;
	public String clef;
	public String key;
	public ArrayList<Symbol> symbols;
	public int upperBound, lowerBound;
	SymbolDictionary myDictionary;

	public static String[] sharpKeySignatures = new String[] { "C major",
			"G major", "D major", "A major", "E major", "B major", "F flat major",
			"C flat major" };
	public static String[] flatKeySignatures = new String[] { "C major",
			"F major", "B flat major", "E flat major", "A flat major",
			"D flat major", "G flat major", "C flat major" };

	/**
	 * Represents one row of staves.
	 * 
	 * @param page
	 *            The full page image of the page this MeasureRow is from.
	 * @param bound1
	 *            The upper boundary of the row as given by Page.measureRows()
	 * @param bound2
	 *            The lower boundary of the row as given by Page.measureRows()
	 * @param staff
	 *            The array of the top and bottom of each staff line as given by
	 *            Page.staffLineYCoords()
	 */
	public MeasureRow(MusicImage page, int bound1, int bound2, int[][] staff, SymbolDictionary dict) {
		upperBound = bound1;
		lowerBound = bound2;
		originalPage = page;
		toTakeSymbolsFrom = originalPage.getSubMusicImage(staffStartX, upperBound,
				originalPage.getWidth(), lowerBound - upperBound);
		staffLines = staff;
		staffStartX = findStaffStart();
		staffEndX = findStaffEnd();
		eraseLines();
		symbols = new ArrayList<Symbol>();
		myDictionary = dict;
	}

	public int findStaffStart() {
		boolean start = true;
		for (int x = 0; x < toTakeSymbolsFrom.getWidth(); x++) {
			start = true;
			for (int[] line : staffLines) {
				if (!toTakeSymbolsFrom.isAllBlackInArea(x, line[0], 1, line[1] - line[0]))
					start = false;
			}
			if (start
					&& !toTakeSymbolsFrom.isAllBlackInArea(x, staffLines[0][0], 1,
							staffLines[4][1] - staffLines[0][0])) {
				return x;
			}
		}
		return -1;
	}

	public int findStaffEnd() {
		for (int x = toTakeSymbolsFrom.getWidth() - 1; x >= 0; x--) {
			boolean start = true;
			for (int[] line : staffLines) {
				if (!toTakeSymbolsFrom.isAllBlackInArea(x, line[0], 1, line[1] - line[0]))
					start = false;
			}
			if (start)
				return x;
		}
		return -1;
	}

	public void eraseLines() {
		for (int x = 0; x < toTakeSymbolsFrom.getWidth(); x++) {
			for (int[] line : staffLines) {
				if (!toTakeSymbolsFrom.blackAboveOrBelow(x, line[0], line[1])) {
					for (int eraseY = line[0]; eraseY>=0&& eraseY <= line[1]&&eraseY<toTakeSymbolsFrom.getHeight(); eraseY++) {
						toTakeSymbolsFrom.setRGB(x, eraseY, 0xFFFFFF);
						toTakeSymbolsFrom.setRGB(x, eraseY, 0xFFFFFF);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param x
	 *            Left edge of the symbol
	 * @param y
	 *            Any y coordinate such that x,y is on the symbol.
	 * @return Returns the symbol at the coordinates and removes the symbol from
	 *         toTakeSymbolsFrom.
	 */
	public Symbol symbolAt(int x, int y) {
		MusicImage symbolImg = new MusicImage(toTakeSymbolsFrom.getWidth(),
				toTakeSymbolsFrom.getHeight(), MusicImage.TYPE_INT_RGB);
		ArrayList<Integer[]> pointsToFill = new ArrayList<Integer[]>();
		Integer[] firstPair = new Integer[] { x, y };
		pointsToFill.add(new Integer[] { x, y });

		int minX = x;
		int minY = y;
		int maxX = x;
		int maxY = y;

		// Floodfill to find the edges of the symbol.
		while (!pointsToFill.isEmpty()) {
			if ((pointsToFill.get(0)[0] >= 0)
					&& (pointsToFill.get(0)[0] < toTakeSymbolsFrom.getWidth())
					&& (pointsToFill.get(0)[1] >= 0)
					&& (pointsToFill.get(0)[1] < toTakeSymbolsFrom.getHeight())
					&& toTakeSymbolsFrom.isBlack(pointsToFill.get(0)[0],
							pointsToFill.get(0)[1])) {
				if (symbolImg.isBlack(pointsToFill.get(0)[0],
						pointsToFill.get(0)[1])) {
					symbolImg.setRGB(pointsToFill.get(0)[0],
							pointsToFill.get(0)[1], 0xFFFFFF);

					if (minX > pointsToFill.get(0)[0])
						minX = pointsToFill.get(0)[0];
					if (minY > pointsToFill.get(0)[1])
						minY = pointsToFill.get(0)[1];

					if (maxX < pointsToFill.get(0)[0])
						maxX = pointsToFill.get(0)[0];
					if (maxY < pointsToFill.get(0)[1])
						maxY = pointsToFill.get(0)[1];
					int pointX = pointsToFill.get(0)[0];
					int pointY = pointsToFill.get(0)[1];

					pointsToFill.add(new Integer[] { pointX - 1, pointY - 1 });
					pointsToFill.add(new Integer[] { pointX, pointY - 1 });
					pointsToFill.add(new Integer[] { pointX + 1, pointY - 1 });

					pointsToFill.add(new Integer[] { pointX - 1, pointY });
					pointsToFill.add(new Integer[] { pointX, pointY });
					pointsToFill.add(new Integer[] { pointX + 1, pointY });

					pointsToFill.add(new Integer[] { pointX - 1, pointY + 1 });
					pointsToFill.add(new Integer[] { pointX, pointY + 1 });
					pointsToFill.add(new Integer[] { pointX + 1, pointY + 1 });
				}
			}

			pointsToFill.remove(0);

		}
		for (int fillX = Math.max(0, minX - 1); fillX < Math.min(maxX + 1,
				symbolImg.getWidth()); fillX++) {
			for (int fillY = Math.max(minY - 1, 0); fillY < Math.min(maxY + 1,
					symbolImg.getHeight()); fillY++) {
				symbolImg.setRGB(fillX, fillY, 0xFFFFFF);
			}
		}
		pointsToFill.add(firstPair);
		/* Floodfill. Erase each symbol from
		   toTakeSymbolsFrom as it is found
		   to keep symbols from being repeated.*/
		while (!pointsToFill.isEmpty()) {
			if ((pointsToFill.get(0)[0] >= 0)
					&& (pointsToFill.get(0)[0] < toTakeSymbolsFrom.getWidth())
					&& (pointsToFill.get(0)[1] >= 0)
					&& (pointsToFill.get(0)[1] < toTakeSymbolsFrom.getHeight())
					&& toTakeSymbolsFrom.isBlack(pointsToFill.get(0)[0],
							pointsToFill.get(0)[1])) {
				symbolImg.setRGB(pointsToFill.get(0)[0],
						pointsToFill.get(0)[1], 0x000000);
				toTakeSymbolsFrom.setRGB(pointsToFill.get(0)[0],
						pointsToFill.get(0)[1], 0xffffff);

				int pointX = pointsToFill.get(0)[0];
				int pointY = pointsToFill.get(0)[1];
				pointsToFill.remove(0);
				pointsToFill.add(new Integer[] { pointX - 1, pointY - 1 });
				pointsToFill.add(new Integer[] { pointX, pointY - 1 });
				pointsToFill.add(new Integer[] { pointX + 1, pointY - 1 });

				pointsToFill.add(new Integer[] { pointX - 1, pointY });
				pointsToFill.add(new Integer[] { pointX, pointY });
				pointsToFill.add(new Integer[] { pointX + 1, pointY });

				pointsToFill.add(new Integer[] { pointX - 1, pointY + 1 });
				pointsToFill.add(new Integer[] { pointX, pointY + 1 });
				pointsToFill.add(new Integer[] { pointX + 1, pointY + 1 });
			}
			else {
				pointsToFill.remove(0);
			}
		}
		
		symbolImg = symbolImg.getSubMusicImage(minX,
				minY, maxX - minX + 1, maxY - minY + 1);
		
		Symbol sym = new Symbol(minX, minY, symbolImg,
				(staffLines[4][1] - staffLines[0][0]) / 4);

		if (!sym.myImage.isBlackInArea(0, 0, sym.myImage.getWidth(),
				sym.myImage.getHeight())) 
			return null;

		return sym;
	}

	public Symbol getFirstSymbolPast(int startX, int startY) {
		for (int x = startX; x < staffEndX + 10
				&& x < toTakeSymbolsFrom.getWidth(); x++) {
			for (int y = startY; y < toTakeSymbolsFrom.getHeight(); y++) {
				if (toTakeSymbolsFrom.isBlack(x, y))
					return symbolAt(x, y);
			}
			startY = 0;
		}
		return null;
	}

	public void getAllSymbols() {
		symbols = new ArrayList<Symbol>();
		try {
			getClef();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(symbols.size()==0)
				return;
			Symbol retrievedSymbol = getFirstSymbolPast(symbols.get(0).staffX
					+ symbols.get(0).myImage.getWidth() + 5, 0);
			while (retrievedSymbol != null) {
				symbols.add(retrievedSymbol);
				retrievedSymbol = getFirstSymbolPast(retrievedSymbol.staffX,
						retrievedSymbol.staffY);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Goes through symbols and revises types based on position and surrounding
	 * symbols.
	 * 
	 * @throws Exception
	 */
	public void interpretSymbols() throws Exception {
		int i = 1;
		if(symbols.size()==0)
			return;
		if ((symbols.get(0).staffY < staffLines[0][0]) != ((symbols.get(0).staffY + symbols
				.get(0).myImage.getHeight()) > staffLines[4][1])
				|| symbols.get(0).myImage.getWidth() < symbols.get(0).staffDist * 2) {
			symbols.get(0).type = myDictionary.interpretSymbol(symbols.get(0));
			// Check for an alto clef
			if (symbols.size() > 3
					&& Math.abs(symbols.get(0).myImage.getHeight()
							- symbols.get(0).staffDist * 4) < symbols.get(0).staffDist) {
				if ((symbols.get(2).staffX + symbols.get(2).myImage.getWidth() - symbols
						.get(0).staffX) < symbols.get(0).staffDist * 6) {
					Symbol altoClef = new Symbol(symbols.get(0).staffX,
							symbols.get(0).staffY, originalPage.getSubMusicImage(
									symbols.get(0).staffX,
									symbols.get(0).staffY,
									symbols.get(2).staffX
											+ symbols.get(2).myImage.getWidth()
											- symbols.get(0).staffX,
									symbols.get(0).myImage.getHeight()),
							symbols.get(0).staffDist);
					altoClef.type = "Alto";
					symbols.remove(0);
					symbols.remove(0);
					symbols.remove(0);
					symbols.add(0, altoClef);
					clef = "Alto";
				}
			} else {
				clef = "Previous row";
				i = 0;
			}
		}
		while (i < symbols.size() && symbols.get(i).staffX < staffEndX) {
			Symbol s = symbols.get(i);
			if (s.staffY < staffLines[0][0]
					|| s.staffY + s.myImage.getHeight() > staffLines[4][1]) {
				s.eraseExtraLines(staffLines[0][0], staffLines[4][1]);
				if (s.type == null)
					s.type = "None";
				if (s.noteHeads.size() == 0)
					s.setNoteHeads();
				s.combineDuplicateNoteHeads();
				// if a notehead has been incorrectly identified
				if (s.noteHeads.size() == 1
						&& s.myImage.getHeight() > s.staffDist * 4 * 1.75) {
					s.noteHeads.remove(0);
					s.type = myDictionary.interpretSymbol(s);
				}
			}
			if (s.noteHeads.size() == 1 && s.noteHeads.get(0)[2] == 0)
				s.type = "Half note";

			else if (s.noteHeads.size() == 1
					&& (s.type.equals("None") || s.type == null))
				s.type = myDictionary.interpretNonbeamedNote(s);

			if (s.noteHeads.isEmpty())
				s.type = myDictionary.interpretSymbol(s);

			if (i > 0 && s.type.equals("None")
					&& symbols.get(i - 1).type.equals("None")
					&& s.noteHeads.isEmpty()) {
				Symbol r = reconstruct(symbols.get(i - 1), s);

				if (r != null) {
					symbols.remove(i - 1);
					symbols.remove(i - 1);
					symbols.add(i - 1, r);

					i--;
				}
			}

			// Split beamed notes apart
			else if (s.noteHeads.size() > 1) {
				ArrayList<Symbol> split = s.separateSymbol();
				symbols.remove(i);
				for (Symbol newSymbol : split) {
					symbols.add(i, newSymbol);
					if (newSymbol.noteHeads.get(0)[1] + newSymbol.staffY < staffLines[0][0]
							- newSymbol.staffDist / 4
							|| newSymbol.noteHeads.get(0)[1] + newSymbol.staffY > staffLines[4][1]
									+ newSymbol.staffDist / 4)
						newSymbol.eraseExtraLines(staffLines[0][0],
								staffLines[4][1]);
					newSymbol.type = myDictionary
							.interpretBeamedNote(newSymbol);
					i++;
				}
				i--;
			}

			if (Math.abs((double) s.myImage.getHeight()
					/ (staffLines[4][1] - staffLines[0][0]) - 1) < .2
					&& s.myImage.getWidth() < s.staffDist) {
				if (s.myImage.isBlackInArea(0, 0, s.myImage.getWidth(),
						s.myImage.getHeight()))
					s.type = "Measure end";
			}

			if (s.type == null)
				s.type = "None";

			if (s.type.equals("Dot")) {
				if (i > 0 && symbols.get(i - 1).type.equals("Dot")
						&& (symbols.get(i - 2).type.equals("Measure end"))) {
					MusicImage repeatStart = originalPage.getSubMusicImage(
							symbols.get(i - 2).staffX,
							symbols.get(i - 2).staffY,
							s.staffX + s.myImage.getWidth()
									- symbols.get(i - 2).staffX,
							symbols.get(i - 2).myImage.getHeight());
					Symbol rep = new Symbol(symbols.get(i - 2).staffX,
							symbols.get(i - 2).staffY, repeatStart,
							(staffLines[4][1] - staffLines[0][0]) / 4);
					rep.type = "Repeat start";
					symbols.remove(i - 2);
					symbols.remove(i - 2);
					symbols.remove(i - 2);
					symbols.add(i - 2, rep);
					i -= 2;
				} else {
					/* If any dots are out of order because
					   they were in the middle of beamed notes,
					   move them to their proper position */
					while (i>=1 && s.staffX < symbols.get(i - 1).staffX) {
						symbols.remove(i);
						symbols.add(i - 1, s);
						i--;
					}
				}
			}

			if (s.type.equals("half or whole rest")) {
				if (s.staffY > staffLines[0][1] && s.staffY < staffLines[1][1])
					s.type = "Whole rest";
				else if (s.staffY > staffLines[1][1]
						&& s.staffY + s.myImage.getHeight() < staffLines[3][0])
					s.type = "Half rest";
				else
					s.type = "None";
			}
			/* Rests cannot be outside the staff
			   so any rests outside the staff
			   have been incorrectly identified */
			if (s.type.contains("rest")
					&& (s.staffY < staffLines[0][1] || s.staffY
							+ s.myImage.getHeight() > staffLines[4][0]))
				s.type = "None";

			if (s.type.equals("Measure end")) {
				if (i > 0 && symbols.get(i - 1).type.equals("Dot")
						&& symbols.get(i - 2).type.equals("Dot")) {
					MusicImage repeatEnd = originalPage.getSubMusicImage(
							symbols.get(i - 2).staffX, s.staffY, s.staffX
									+ s.myImage.getWidth()
									- symbols.get(i - 2).staffX,
							s.myImage.getHeight());
					Symbol rep = new Symbol(symbols.get(i - 2).staffX,
							s.staffY, repeatEnd,
							(staffLines[4][1] - staffLines[0][0]) / 4);
					rep.type = "Repeat end";
					symbols.remove(i - 2);
					symbols.remove(i - 2);
					symbols.remove(i - 2);
					symbols.add(i - 2, rep);
					i -= 2;
				}
			}

			if (s.type.equals("Whole note")) {
				s.noteHeads.add(new int[] { s.myImage.getWidth() / 2,
						s.myImage.getHeight() / 2, -1 });
			}
			i++;
		}

		for (int k = 0; k < symbols.size(); k++) {
			if (symbols.get(k).type == null)
				symbols.get(k).type = "None";
		}

		// Find ties
		try {
			for (int k = 2; k < symbols.size() - 1; k++) {
				if (symbols.get(k).type.equals("None")) {
					if (symbols.get(k - 1).type.contains("note")
							|| symbols.get(k + 1).type.contains("note")
							|| (k > 1 && symbols.get(k - 2).type
									.contains("note"))) {
						if (Math.abs(symbols.get(k - 1).staffX
								+ symbols.get(k - 1).myImage.getWidth()
								- symbols.get(k).staffX) < symbols.get(k - 1).myImage
								.getWidth()) {
							if (myDictionary.isTie(symbols.get(k)))
								symbols.get(k).type = "Tie";
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void getClef() throws IOException {
		Symbol c = getFirstSymbolPast(staffStartX + 1, staffLines[1][0]
				+ (staffLines[4][1] - staffLines[0][0]) / 8);
		if (c.staffX > staffStartX)
			symbols.add(c);

		if (c.staffX < staffStartX
				|| ((c.staffY < staffLines[0][0] && c.staffY
						+ c.myImage.getHeight() < staffLines[0][0]) || (c.staffY > staffLines[4][1] && c.staffY
						+ c.myImage.getHeight() > staffLines[4][1]))) {
			if (symbols.size() > 0)
				symbols.get(0).type = myDictionary.interpretSymbol(symbols
						.get(0));
			c = getFirstSymbolPast(staffStartX + 1, staffLines[1][0]
					+ (staffLines[4][1] - staffLines[0][0]) / 8);
			symbols.add(c);
		}

		clef = myDictionary.interpretClef(c);
		c.type = clef + " clef";

	}

	//prevent accidentals from being incorrectly read
	//as signifying a key signature
	public Boolean isAccidental(Symbol accidental, Symbol next) {
		if(!next.type.contains("note")) return false;
		return accidental.staffX + accidental.myImage.getWidth() * 1.5 > next.staffX;
	}

	/**
	 * Evaluates and sets the key signature. Only run after getAllSymbols() and
	 * interpretSymbols()
	 */
	public void getKeySignature() {
		int sharps = 0;
		int flats = 0;
		for (int i = 0; i < symbols.size() - 1; i++) {
			if (symbols.get(i).staffX + symbols.get(i).myImage.getWidth() > staffStartX) {
				if (symbols.get(i).type.equals("Sharp")&&
					!isAccidental(symbols.get(i),symbols.get(i+1))) {
					sharps = sharps + 1;
				}
				else if (symbols.get(i).type.equals("Flat")&&
					!isAccidental(symbols.get(i),symbols.get(i+1))) {
					flats = flats + 1;
				}
				else if (!symbols.get(i).type.contains("clef")) {
					if (flats > 0)
						key = flatKeySignatures[flats];
					else 
						key = sharpKeySignatures[sharps];
					return;
				}
			}
		}
	}

	/**
	 * Symbols are sometimes split apart when the staff lines are erased.
	 * 
	 * @param sym1
	 *            The leftmost part of the possibly split symbol.
	 * @param sym2
	 *            The rightmost part of the possibly split symbol.
	 * @return A new Symbol if sym1 and sym2 were a split symbol. Else returns
	 *         null.
	 */
	public Symbol reconstruct(Symbol sym1, Symbol sym2) {
		int topY;
		int bottomY;
		int rightX;
		int leftX;

		topY = Math.min(sym1.staffY, sym2.staffY);
		bottomY = Math.max(sym1.staffY + sym1.myImage.getHeight(), sym2.staffY
				+ sym2.myImage.getHeight());
		rightX = Math.max(sym1.staffX + sym1.myImage.getWidth(), sym2.staffX
				+ sym2.myImage.getWidth());
		leftX = Math.min(sym1.staffX, sym2.staffX);
		Symbol reconstructed = new Symbol(leftX, topY, sym2.myImage,
				(staffLines[4][1] - staffLines[0][0]) / 4);
		
		if (reconstructed.staffY < staffLines[0][0]
				|| reconstructed.staffY + reconstructed.myImage.getHeight() > staffLines[4][1])
			reconstructed.eraseExtraLines(staffLines[0][0], staffLines[4][1]);

		reconstructed.type = myDictionary.interpretReconstructedSymbol(sym1,
				sym2, reconstructed);
		if (!reconstructed.type.equals("None") && !(reconstructed.type == null))
			return reconstructed;

		return null;
	}

	/**
	 * Returns the distance on the staff from middle C (e.g. B is 1, D is -1)
	 */
	public int evalPitch(int y) {
		double distFromTopLine = (y - (double) staffLines[0][0]) * 8
				/ (staffLines[4][0] - staffLines[0][0]) - .4;
		if (Math.abs(distFromTopLine) % 1 > .52)
			distFromTopLine = Math.round(distFromTopLine);
		else
			distFromTopLine = (int) distFromTopLine;
		if (clef.equals("Alto"))
			return (int) Math.round(distFromTopLine) - 4;
		else if (clef.equals("Bass"))
			return (int) Math.round(distFromTopLine) + 2;
		else 
			return (int) Math.round(distFromTopLine) - 10;
	}
}
