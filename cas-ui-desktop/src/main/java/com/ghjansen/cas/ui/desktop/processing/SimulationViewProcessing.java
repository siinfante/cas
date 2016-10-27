/*
 * CAS - Cellular Automata Simulator
 * Copyright (C) 2016  Guilherme Humberto Jansen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ghjansen.cas.ui.desktop.processing;

import java.util.List;

import processing.core.PApplet;

import com.ghjansen.cas.unidimensional.physics.UnidimensionalCell;
import com.ghjansen.cas.unidimensional.physics.UnidimensionalUniverse;

/**
 * @author Guilherme Humberto Jansen (contact.ghjansen@gmail.com)
 */
public class SimulationViewProcessing extends PApplet {

	private UnidimensionalUniverse universe;
	private TransitionsViewProcessing transitions;
	private ViewCommonsProcessing commons;
	private int width = 582;
	private int height = 582;
	private int background = 204;
	private float squareSize = 100.0F;
	private int x = -1;
	private int y = 0;
	private int feedbackRate = 60;
	private float translationX;
	private float translationY;
	private float minScale = 0.01F;
	private float maxScale = 1.28F;
	private float alphaScale = 2.0F;
	private float deltaScale = 0.0F;
	private float scale = minScale;
	private float lastScale = scale; 
	private float mousePressedX;
	private float mousePressedY;
	private boolean cellInspector = false;
	private float inspectionSubjectX;
	private float inspectionSubjectY;
	private boolean resetControl = true;
	
	public SimulationViewProcessing(ViewCommonsProcessing commons, TransitionsViewProcessing transitions){
		this.commons = commons;
		this.transitions = transitions;
	}

	public void setup() {
		size(width, height);
		textAlign(CENTER);
		background(background);
	}

	public void draw() {
		if (isSpaceAvailable()) {
			drawSpace();
			drawTools();
		} else {
			drawWelcome();
		}
		updateLastScale();
		updateResetControl();
	}
	
	private boolean isSpaceAvailable(){
		return universe != null && universe.getSpace().getInitial().size() > 0;
	}
	
	private void drawSpace(){
		pushMatrix();
		centralizeTranslation();
		updateInspectionSubject();
		translate(translationX, translationY);
		scale(scale);
		strokeControl();
		drawInitialCondition();
		if(universe.getSpace().getHistory().size() > 0){
			drawHistory();
		}
		if(universe.getSpace().getCurrent().size() > 0){
			drawCurrent();
		}
		drawInspector();
		popMatrix();
	}
	
	private void centralizeTranslation(){
		if(resetControl){
			float emptySpaceX = width - universe.getTime().getRelative().get(0).getLimit();
			if(emptySpaceX > 0){
				translationX = emptySpaceX / 2;
			}
			float emptySpaceY = height - universe.getTime().getLimit();
			if(emptySpaceY > 0){
				translationY = emptySpaceY / 2;
			}
		} else if (lastScale != scale){
			float centralShiftX;
			float centralShiftY;
			if(lastScale > scale){
				centralShiftX = ((width / 2) - translationX) / 2;
				centralShiftY = ((height / 2) - translationY) / 2;
				translationX = translationX + centralShiftX;
				translationY = translationY + centralShiftY;
				inspectionSubjectX = inspectionSubjectX + centralShiftX;
				inspectionSubjectY = inspectionSubjectY + centralShiftY;
			} else {
				centralShiftX = (((width / 2) - translationX));
				centralShiftY = (((height / 2) - translationY));
				translationX = translationX - centralShiftX;
				translationY = translationY - centralShiftY;
				inspectionSubjectX = inspectionSubjectX - centralShiftX;
				inspectionSubjectY = inspectionSubjectY - centralShiftY;				
			}
		}
	}
	
	private void drawInitialCondition(){
		List<?> initialCondition = universe.getSpace().getInitial();
		for(int i = 0; i < initialCondition.size(); i++){
			UnidimensionalCell c = (UnidimensionalCell) initialCondition.get(i);
			if (c.getState().getValue() == 0) {
				fill(255);
			} else if (c.getState().getValue() == 1) {
				fill(0);
			}
			rect(squareSize * i, 1, squareSize, squareSize);
		}
	}
	
	private void strokeControl(){
		if(scale > 0.16){
			stroke(background);
			strokeWeight(1);
		} else {
			strokeWeight(0);
			noStroke();
		}
	}
	
	private void drawHistory() {
		List<List> history = universe.getSpace().getHistory();
		// Draw all complete lines
		int iterations = 0;
		while (history.size() > y && iterations < feedbackRate) {
			while (history.get(y).size() - 1 > x) {
				nextColumn();
				drawCell((UnidimensionalCell) history.get(y).get(x));
			}
			nextLine();
			iterations++;
		}
		// Draw incomplete or last line
		if (history.size() - 1 == y && history.size() - 1 > y) {
			while (history.get(y).size() - 1 > x) {
				nextColumn();
				drawCell((UnidimensionalCell) history.get(y).get(x));
			}
		}
	}
	
	private void drawCurrent(){
		List<?> current = universe.getSpace().getCurrent();
		for(int i = 0; i < current.size(); i++){
			UnidimensionalCell c = (UnidimensionalCell) current.get(i);
			if (c.getState().getValue() == 0) {
				fill(255);
			} else if (c.getState().getValue() == 1) {
				fill(0);
			}
			rect(squareSize * i, squareSize * (y + 1), squareSize, squareSize);
		}
	}
	
	private void drawInspector(){
		if(cellInspector){
			cursor(CROSS);
			float scaledSquareSize = scale * squareSize;
			float floatCellX = (inspectionSubjectX - translationX) / scaledSquareSize;
			float floatCellY = (inspectionSubjectY - translationY) / scaledSquareSize;
			int intCellX = (int) floatCellX;
			int intCellY = (int) floatCellY;
			drawInspectorBorders(intCellX, intCellY);
		} else {
			cursor(MOVE);
		}
	}
	
	private void drawInspectorBorders(int xCell, int yCell){
		System.out.println("xCell: "+ xCell + " yCell:"+yCell);
		int absoluteTimeLimit = universe.getTime().getLimit();
		int relativeTimeLimit = universe.getTime().getRelative().get(0).getLimit();
		if(xCell >= 0 && xCell < relativeTimeLimit && 
				yCell > 0 && yCell < absoluteTimeLimit + 1){
			
			commons.glowControl();
			stroke(255.0F, commons.glowIntensity, commons.glowIntensity);
			strokeWeight(squareSize/5);
			strokeCap(ROUND);
			noFill();
			
			float cx = xCell * squareSize;
			float cy = yCell * squareSize;
			float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y, p5x, p5y, p6x, p6y, p7x, p7y, p8x, p8y, p9x, p9y, p10x, p10y;
			
			/**
			 * P1---P2---P3---P4
			 * |    |    |    |
			 * P10--P9---P6---P5
			 *      |    |
			 *      P8---P7
			 */
			
			p2x = cx;
			p2y = cy - squareSize;
			p3x = cx + squareSize;
			p3y = cy - squareSize;
			p6x = cx + squareSize;
			p6y = cy;
			p7x = cx + squareSize;
			p7y = cy + squareSize;
			p8x = cx;
			p8y = cy + squareSize;
			p9x = cx;
			p9y = cy;
			
			
			if(xCell == 0){ 
				// extreme left, include combination cell from right side
				p1x = cx + (squareSize * relativeTimeLimit) - squareSize; //different
				p1y = cy - squareSize;
				p4x = cx + (squareSize * 2);
				p4y = cy - squareSize;
				p5x = cx + (squareSize * 2);
				p5y = cy;
				p10x = cx + (squareSize * relativeTimeLimit) - squareSize; //different
				p10y = cy;
				line(p1x, p1y, p1x + squareSize, p2y); //different
				line(p2x, p2y, p3x, p3y);
				line(p3x, p3y, p4x, p4y);
				line(p4x, p4y, p5x, p5y);
				line(p5x, p5y, p6x, p6y);
				line(p6x, p6y, p7x, p7y);
				line(p7x, p7y, p8x, p8y);
				line(p8x, p8y, p9x, p9y);
				line(p10x, p10y, p10x + squareSize, p10y); //different
				line(p10x, p10y, p1x, p1y);
				
			} else if (xCell == relativeTimeLimit - 1){
				// extreme right, include combination cell from left side
				p1x = cx - squareSize;
				p1y = cy - squareSize;
				p4x = squareSize; //different
				p4y = cy - squareSize;
				p5x = squareSize; //different
				p5y = cy;
				p10x = cx - squareSize;
				p10y = cy;
				line(p1x, p1y, p2x, p2y);
				line(p2x, p2y, p3x, p3y);
				line(0, p4y, p4x, p4y); //different
				line(p4x, p4y, p5x, p5y);
				line(p5x, p5y, 0, p5y); //different
				line(p6x, p6y, p7x, p7y);
				line(p7x, p7y, p8x, p8y);
				line(p8x, p8y, p9x, p9y);
				line(p9x, p9y, p10x, p10y);
				line(p10x, p10y, p1x, p1y);
			} else {
				// regular mid location, draw borders normally
				p1x = cx - squareSize;
				p1y = cy - squareSize;
				p4x = cx + (squareSize * 2);
				p4y = cy - squareSize;
				p5x = cx + (squareSize * 2);
				p5y = cy;
				p10x = cx - squareSize;
				p10y = cy;
				line(p1x, p1y, p2x, p2y);
				line(p2x, p2y, p3x, p3y);
				line(p3x, p3y, p4x, p4y);
				line(p4x, p4y, p5x, p5y);
				line(p5x, p5y, p6x, p6y);
				line(p6x, p6y, p7x, p7y);
				line(p7x, p7y, p8x, p8y);
				line(p8x, p8y, p9x, p9y);
				line(p9x, p9y, p10x, p10y);
				line(p10x, p10y, p1x, p1y);
			}
			
			
			
		}
	}
	
	private void updateInspectionSubject(){
		if(lastScale != scale){
			if(lastScale > scale){
				inspectionSubjectX = ((inspectionSubjectX - translationX) / alphaScale) + translationX;
				inspectionSubjectY = ((inspectionSubjectY - translationY) / alphaScale) + translationY;
			} else {
				inspectionSubjectX = ((inspectionSubjectX - translationX) * alphaScale) + translationX;
				inspectionSubjectY = ((inspectionSubjectY - translationY) * alphaScale) + translationY;
			}
		}
	}

	private void drawTools() {

	}

	private void drawWelcome() {
		textSize(25);
		text("Mensagem de boas vindas aqui!", 291, 291);
	}
	
	private void updateLastScale(){
		lastScale = scale;
	}
	
	private void updateResetControl(){
		resetControl = false;
	}
	
	private void drawCell(UnidimensionalCell c) {
		if (c.getState().getValue() == 0) {
			fill(255);
		} else if (c.getState().getValue() == 1) {
			fill(0);
		}
		rect(squareSize * x, squareSize * (y + 1), squareSize, squareSize);
	}

	private void nextColumn() {
		x++;
	}

	private void nextLine() {
		y++;
		x = -1;
	}

	public void setUniverse(UnidimensionalUniverse universe) {
		this.universe = universe;
	}
	
	public void reset(){
		translationX = 0.0F;
		translationY = 0.0F;
		scale = minScale;
		lastScale = scale;
		deltaScale = 0.0F;
		cellInspector = false;
		resetControl = true;
		refresh();
	}
	
	private void refresh(){
		x = -1;
		y = 0;
		background(background);
	}
	
	public void mousePressed(){
		mousePressedX = mouseX;
		mousePressedY = mouseY;
	}
	
	public void mouseReleased(){
		if(cellInspector){
			inspectionSubjectX = mousePressedX;
			inspectionSubjectY = mousePressedY;
		} else {
			translationX = translationX + (mouseX - mousePressedX);
			translationY = translationY + (mouseY - mousePressedY);
			inspectionSubjectX = inspectionSubjectX + (mouseX - mousePressedX);
			inspectionSubjectY = inspectionSubjectY + (mouseY - mousePressedY);
		}
		refresh();
	}
	
	public void keyPressed(){
		if(key == '1' && scale > minScale){
			lastScale = scale;
			double pow = --deltaScale;
			scale = minScale * (float) Math.pow( (double) alphaScale, pow);
			refresh();
		} else if (key == '2' && scale < maxScale){
			lastScale = scale;
			double pow = ++deltaScale;
			scale = minScale * (float) Math.pow( (double) alphaScale, pow);
			refresh();
		} else if (key == '3') {
			if(cellInspector){
				cellInspector = false;
				refresh();
			} else {
				cellInspector = true;
			}
		}
	}
}
