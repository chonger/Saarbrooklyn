package saarbrooklyn;

import java.awt.Color;

import gifAnimation.Gif;
import controlP5.Bang;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.DropdownList;
import controlP5.Label;
import controlP5.Textarea;
import controlP5.Textfield;
import controlP5.Textlabel;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.shapes.Box;
import pointclick.DialogGNode;
import pointclick.NPC;
import pointclick.NPCAction;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import scala.Array;
import scala.Tuple2;
import scala.Tuple3;

public class SBDialogNode {

	DropdownList triggers = null;
	Button editTriggerButton = null;
	Button jumpTriggerButton = null;
	ControlWindow triggerEditWin = null;
	DropdownList newTrigger1 = null;
	DropdownList newTrigger2 = null;
	Button makeTrigger = null;
	Button cancelTrigger = null;
	Button deleteTrigger = null;
	Integer triggerEditIndex = null;
	DropdownList triggerCondList = null;
	DropdownList triggerCond1 = null;
	DropdownList triggerCond2 = null;
	Button addTriggerCond = null;
	Button delTriggerCond = null;
	Button linkTrigger = null;
	Textlabel triggerLink = null;
	
	DropdownList effects = null;
	Bang newEffect;
	Bang delEffect;
	
	
	
	DropdownList editeffect1 = null;
	DropdownList editeffect2 = null;
	

	
	
	Saarbrooklyn sb;
	DialogGNode dnode;
	DialogGNode[] dgraph;
	Body[] bodies;
	NPC npc;
	float sayX = 350;
	float sayY = 250;
	Body hoverBody;
	String[] responses;
	String[] pars;
	float inc = 60;
	float rX = 533;
	float rY = 0; 
	float pX = 10;
	float pY = 0;
	Integer editInd = null;
	boolean linking = false;
	int linkR = 0;
	Integer linkHover = null;
	boolean actionNav = false;
	String savedTrigger = null;
	boolean addSomething = false;
	
	Body[] effectBodies;
	Body[] imgBodies;
	
	Tuple3<String,NPCAction,Tuple2<String,String>[] >[] ts;
	
	NonPlayer npr;

	boolean npcTurn = false;
	Integer responseIndex = null;
	

	
	public void customize(DropdownList ddl) {
		sb.cp5.setColorForeground(Color.white.getRGB());
		sb.cp5.setColorBackground(Color.black.getRGB());
		sb.cp5.setColorLabel(Color.green.getRGB());
		sb.cp5.setColorValue(Color.blue.getRGB());
		sb.cp5.setColorActive(Color.yellow.getRGB());
		ddl.setBarHeight(20);
		ddl.setItemHeight(30);
		
	}
	
	public SBDialogNode(Saarbrooklyn sb,NPC npc, NonPlayer npr) {
		this.npr = npr;
		this.sb = sb;
		this.npc = npc;
		dgraph = npc.buildGraph();
		hoverBody = null;
			
		refreshTriggers();
		editTriggerButton = sb.cp5.addButton("EDIT", 0, 170, 9, 40, 20);
		jumpTriggerButton = sb.cp5.addButton("GOTO", 0, 215, 9, 40, 20);
		
		//newTrigger = sb.cp5.addBang("add trigger", 170, 10, 20, 20);
		//delTrigger = sb.cp5.addBang("remove trigger", 170, 50, 20, 20);
		setupTriggerEditor();
		setVizOfNode(dgraph[0]);
		sb.cp5.show();
		triggerEditWin.hide();
		editTriggerButton.hide();
		jumpTriggerButton.hide();

	}
	
	public void refreshTriggers() {
		sb.cp5.remove("Select Trigger");
		triggers = sb.cp5.addDropdownList("Select Trigger",10,30,150,100);
		customize(triggers);
		ts = npc.getTriggers();
		for(int i=0;i<ts.length;++i) {
			triggers.addItem(ts[i]._1(),i);
		}
		triggers.addItem("New Trigger",ts.length);
	}
	
	public void jumpToTrigger() {
		int index = (int) triggers.value();
		if(index == ts.length) {
			sb.println("TODO : Can't jump to a new trigger");
		}
		NPCAction tA = ts[(int) triggers.value()]._2();
		int ind = 0;
		for(int i=0;i<dgraph.length;++i) {
			if(dgraph[i].action() == tA)
				ind = i;
		}
		setVizOfNode(dgraph[ind]);
	}
	
	public void openTriggerEditor() {
		//choose action, link, conditions
		int index = (int) triggers.value();
		sb.println("Opening trigger editor with " + index);
		triggerEditIndex = index;
		triggerEditWin.show();
		

		if(triggerEditIndex < ts.length) {
			//initialize stuff
		}
	}
	
	public void setupTriggerEditor() {
		
		triggerEditWin = sb.cp5.addControlWindow("EditTrigger", 100, 100, 600, 300);
		
		triggerEditWin.setBackground(Color.lightGray.getRGB());
		//triggerEditWin.setColorForeground(Color.green.getRGB());
		
		newTrigger1 = sb.cp5.addDropdownList("Pick Action", 10, 30, 100, 200);
		newTrigger1.moveTo(triggerEditWin);
		for(int i=0;i<sb.actions.length;++i) {
			newTrigger1.addItem(sb.actions[i],i);
		}  
		customize(newTrigger1);
		
		makeTrigger = sb.cp5.addButton("Create",0, 400, 260, 55, 30);
		makeTrigger.moveTo(triggerEditWin);
		deleteTrigger = sb.cp5.addButton("Delete",0, 465, 260, 55, 30);
		deleteTrigger.moveTo(triggerEditWin);
		cancelTrigger = sb.cp5.addButton("Cancel",0, 530, 260, 60, 30);
		cancelTrigger.moveTo(triggerEditWin);
		
		triggerCondList = sb.cp5.addDropdownList("Conditions", 400, 30, 100, 100);
		customize(triggerCondList);
		triggerCondList.moveTo(triggerEditWin);
		
		addTriggerCond = sb.cp5.addButton("Add", 0, 360, 5, 35, 25);
		addTriggerCond.moveTo(triggerEditWin);
		
		delTriggerCond = sb.cp5.addButton("Destroy", 0, 510, 5, 70, 25);
		delTriggerCond.moveTo(triggerEditWin);
		
		linkTrigger = sb.cp5.addButton("Edit Link",0,10,210,80,30);
		linkTrigger.moveTo(triggerEditWin);
		
		triggerLink = sb.cp5.addTextlabel("Link", "NONE", 100, 220);
		triggerLink.setColorForeground(Color.black.getRGB());
		triggerLink.moveTo(triggerEditWin);
		
	}
	
	public void handleCP5(ControlEvent theEvent) {
		  // PulldownMenu is if type ControlGroup.
		  // A controlEvent will be triggered from within the ControlGroup.
		  // therefore you need to check the originator of the Event with
		  // if (theEvent.isGroup())
		  // to avoid an error message from controlP5.
			sb.println("CP5 EVENT");
		  if (theEvent.isGroup()) {
			  if(theEvent.group() == triggers) { //jump to that trigger
				  if(triggers.value() == ts.length) { //NEW TRIGGER
					  jumpTriggerButton.hide();
				  } else {
					  jumpTriggerButton.show();
				  }
				  editTriggerButton.show();
				  sb.println("TRIGGER SELECTED");
			  }
			  if(theEvent.group() == newTrigger1) {
				  int ind = (int) theEvent.group().value();
				  String act = sb.actions[ind];
				  if(ind == 7 || ind == 11) {
					  if(newTrigger2 != null) newTrigger2.remove();
					  newTrigger2 = sb.cp5.addDropdownList("Pick Item", 120, 30, 100, 200);
					  customize(newTrigger2);
					  for(int i=0;i<sb.gameState.allInv().length;++i) {
						  newTrigger2.addItem(sb.gameState.allInv()[i],i);
					  }
					  newTrigger2.moveTo(triggerEditWin);
				  } 
			  }
			  /**
			  if(theEvent.group() == editeffect1) {
				  int ind = (int) theEvent.group().value();
				  String act = sb.actions[ind];
				  if(okButton != null) okButton.remove();
				  if(editeffect2 != null) editeffect2.remove();
				  if(editeffect1.value() < 2) {
					  editeffect2 = sb.cp5.addDropdownList("Pick Item", 550, 320, 100, 180);
					  customize(editeffect2);
					  for(int i=0;i<sb.gameState.allInv().length;++i) {
						  editeffect2.addItem(sb.gameState.allInv()[i],i);
					  }
				  } else {
					  editeffect2 = sb.cp5.addDropdownList("Pick Game Key", 550, 320, 100, 180);
					  customize(editeffect2);
					  for(int i=0;i<sb.gameState.allStates().length;++i) {
						  editeffect2.addItem(sb.gameState.allStates()[i],i);
					  }
				  }
			  }
			  if(theEvent.group() == editeffect2) {
				  if(okButton != null) okButton.remove();
				  okButton = sb.cp5.addBang("Create Effect", 370,320, 20, 20);
			  }
			  */
		    //sb.println("CONTROLp5 - " + theEvent.group().value());
		  } else if(theEvent.isController()) {
		   //sb.println(theEvent.controller().value()+" from "+theEvent.controller());
			  if(theEvent.controller() == jumpTriggerButton) {
				  jumpToTrigger();
			  }
			  if(theEvent.controller() == editTriggerButton) {
				  openTriggerEditor();
			  }
			  if(theEvent.controller() == makeTrigger) {
				  sb.println("NEW TRIGGER");
				  
				  if(newTrigger1 != null) newTrigger1.remove();
				  if(newTrigger2 != null) newTrigger2.remove();
				  sb.cp5.remove("Create Trigger");
				  sb.cp5.remove("Pick Item");
				  
				  newTrigger1 = sb.cp5.addDropdownList("Pick Action", 320, 30, 100, 200);
				  for(int i=0;i<sb.actions.length;++i) {
					  newTrigger1.addItem(sb.actions[i],i);
				  }  
				  customize(newTrigger1);
				  addSomething = true;
			  }
			  if(theEvent.controller() == deleteTrigger) {
				  sb.println("DEL TRIGGER");
				  sb.println(triggers.stringValue());
				  triggerEditWin.hide();
				  triggerEditIndex = null;
				  Tuple3<String,NPCAction,Tuple2<String,String>[] > t = ts[(int) triggers.value()];
				  npc.removeTrigger(t._1(),t._3());
				  refreshTriggers();
			  }
			  if(theEvent.controller() == cancelTrigger) {
				  sb.println("Cancel TRIGGER");
				  //triggerEditWin.clear();
				  triggerEditWin.hide();
				  triggerEditIndex = null;
			  }
			  /**
			  if(theEvent.controller() == newEffect) {
				  sb.println("NEW EFFECt");
				  if(editeffect1 != null) editeffect1.remove();
				  editeffect1 = sb.cp5.addDropdownList("Pick Effect Type",210,320,140,180);
				  customize(editeffect1);
				  String[] types =  {"Add Inventory","Rem Inventory","Add Gamestate","Rem Gamestate"};
				  for(int i=0;i<types.length;++i) {
					  editeffect1.addItem(types[i], i);
				  } 
				  addSomething = true;
			  }
			  if(theEvent.controller() == delEffect) {
				  sb.println("DEL EFFECT");
				  dnode.action().removeEffect(dnode.action().effects().apply((int)effects.value()));
				  setVizOfNode(dnode);
			  }
			  */
			  /**
			  if(theEvent.controller() == okButton) {
				  sb.println("OK");
				  if(editeffect1 != null) {
					  sb.println("ADDING EFFECT");
					  String typ = "";
					  switch((int) editeffect1.value()) {
					  case 0: typ = "ADDI"; break;
					  case 1: typ = "REMI"; break;
					  case 2: typ = "ADDS"; break;
					  case 3: typ = "REMS"; break;
					  }
					  
					  dnode.action().addEffect(typ, editeffect2.stringValue());
					  
					  setVizOfNode(dnode);

					  okButton.remove();
					  editeffect1.remove();
					  editeffect2.remove();
					  editeffect1 = null;
					  okButton = null;
					  editeffect2 = null;
					  addSomething = false;
				  }
				  if(sb.cp5.getGroup("Pick Action") != null) {
					  sb.println("ADDING TRIGGER");
					  savedTrigger = newTrigger1.stringValue();
					  sb.println("!" + savedTrigger);
					  if(sb.cp5.getGroup("Pick Action") != null)
						  savedTrigger += "," + newTrigger2.stringValue();
					  
					  sb.println("SAVED TRIGGER is " + savedTrigger);
					 
					  newTrigger1.remove();
					  newTrigger2.remove();
					  okButton.remove();
					  //okButton = null;
					  newTrigger1 = null;
					  newTrigger2 = null;
					  addSomething = false;
				  }
			  }
			  */
		  }
	}
	
	public void remove() {
		if(hoverBody != null) {
			for(int i=1;i<responses.length+1;++i) {
				if(hoverBody == bodies[i]) {
					dnode.removeResponse(i-1);
				}
			}
			setVizOfNode(dnode);
		}
	}
	
	public String formatStr(String s, int maxX, int maxY) {
		int accum = 0;
		String ret = "";
		int y = 0;
		for(int i=0;i<s.length();++i) {
			if(y <= maxY) {
				ret += s.charAt(i);
				accum += 1;
				if(accum == maxX) {
					ret += "\n";
					accum = 0;
					y += 1;
				}
			}
		}
		return ret;
	}
	
	public void link() {
		if(hoverBody != null) {
			int rInd = -1;
			
			for(int i=1;i<responses.length+1;++i) {
				if(hoverBody == bodies[i]) {
					rInd = i-1;
				}
			}
			
			if(rInd >= 0) { //hovering over a response body
				//offer links to all existing nodes, including a new node option
				linking = true;
				linkR = rInd;
				sb.cp5.hide();
			}
		}
	}
	
	public void addResponse() {
		if(dnode.action().responses().length < 5) {
			dnode.addResponse("TEST");	
			setVizOfNode(dnode);
		}
	}
	
	public void setVizOfNode(DialogGNode n) {
		
		dnode = n;
		NPCAction act = n.action();
		
		if(effects != null)
			effects.remove();
		sb.cp5.remove("select effect");
		sb.cp5.remove("remove effect");
		sb.cp5.remove("add effect");
		effects = sb.cp5.addDropdownList("select effect",376,331,100,120);
		customize(effects);
		for(int i=0;i<n.action().effects().length();++i) {
			Tuple2<String,String> e = n.action().effects().apply(i);
			String type = e._1();
			String label = e._2();
			effects.addItem(type + " -- " + label, i);
		}
		newEffect = sb.cp5.addBang("add effect", 490, 321, 20, 20);
		delEffect = sb.cp5.addBang("remove effect", 490, 361, 20, 20);
		
		//sb.println("NR = " + dnode.action().responses().length);
		responses = dnode.action().responseStrings();
		
		int numPars = dnode.parents().length;
		pars = new String[numPars];
		for(int i=0;i<numPars;++i) {
			int ind = (dnode.parents()[i])._2();
			pars[i] = (dnode.parents()[i])._1().action().responses()[ind]._1();
		}
		
		int numR = responses.length;
		//sb.println("NUM R " + numR);
		rY = sb.height/2f - numR/2f * inc;
		pY = sb.height/2f - numPars/2f * inc;
		bodies = new Body[numR+numPars+1];
		bodies[0] = new StaticBody(new Box(60,60));
		bodies[0].setPosition(sayX, sayY);
		
		float y = rY;
		for(int i=1;i<numR+1;++i) {
			bodies[i] = new StaticBody(new Box(50,50));
			bodies[i].setPosition(rX, y);
			y += inc;
		}
		y = pY;
		for(int i=numR+1;i<numR+numPars+1;++i) {
			bodies[i] = new StaticBody(new Box(50,50));
			bodies[i].setPosition(pX, y);
			y += inc;
		}
	}
	
	String editString = null; 
	public void startEdit() {
		if(hoverBody != null) {
			for(int i=0;i<bodies.length;++i) {
				if(hoverBody == bodies[i]) {
					editInd = new Integer(i);
					if(i == 0) 
						editString = dnode.action().say();
					else {
						if(i < responses.length+1)
							editString = responses[i-1];
						else
							editString = pars[i-1-responses.length];
					}
				}
			}
		}
	}
	
	public void endEdit() {
		if(editInd != null) {
			//sb.println("DONE EDITING! -" + editString);
			if(editInd == 0) {
				dnode.action().setSay(editString);
			} else {
				if(editInd < responses.length + 1) {
					dnode.action().setResponse(editString,editInd-1);
					responses[editInd -1] = editString;
				} else {
					int ind = dnode.parents()[editInd-responses.length-1]._2();
					dnode.parents()[editInd]._1().action().setResponse(editString,ind);
					pars[editInd -responses.length-1] = editString;
				}
			}
			editInd = null;
		}
	}
	
	public boolean validChar(char k) {
		return (Character.isLetter(k) || k == '.' || 
				k == "\u0027".charAt(0) || k == '?' ||
				k == ',' || k == '!' || k == ' ');
	}
	
	public void handleKey(char key) {
		if(editString != null) {
		
			if(sb.keyCode == PConstants.BACKSPACE) {
				if(editString.length() > 0)
					editString = editString.substring(0, editString.length() - 1);
			} else {
				if(validChar(key))
					editString += key;
			}
			
			//fix edit string
			String[] parts = editString.split("\\s");
			//sb.println(" NPOARTS - " + parts.length);
			String newES = "";
			int accum = 0;
			for(int i=0;i<parts.length;++i) {
				accum += parts[i].length() + 1;
				//sb.println(accum);
				if(accum > 50) {
					accum = 0;
					newES += parts[i] + "\n";
				} else {
					newES += parts[i] + " ";
				}
			}
			if(editString.length() > 0 && editString.charAt(editString.length()-1) != ' ')
				editString = newES.substring(0,newES.length()-1);
			
			//sb.println(editString);
		}
	}
	
	public boolean checkBod(Body b, float x, float y) {
		float cx = b.getPosition().getX();
		float cy = b.getPosition().getY();
		float w = ((Box) b.getShape()).getSize().getX();
		float h = ((Box) b.getShape()).getSize().getY();
		if(x > cx - w/2 &&
				x < cx + w/2 &&
				y > cy - h/2 &&
				y < cy + h/2) {
			return true;
		}
		return false;
	}
	
	public void click() {
		if(actionNav) {
			if(linkHover != null) {
				DialogGNode gn = null;
				if(linkHover == dgraph.length) { // new action
					NPCAction a = npc.addAction();
					gn = new DialogGNode(a);
					DialogGNode[] dg2 = new DialogGNode[dgraph.length + 1];
					PApplet.arrayCopy(dgraph,0,dg2,0,dgraph.length);
					dg2[dgraph.length] = gn;
					dgraph = dg2;
				} else {
					gn = dgraph[linkHover];
				}
				setVizOfNode(gn);
				if(savedTrigger != null) {
					npc.addTrigger(savedTrigger, gn.action());
					refreshTriggers();
					sb.println("NEW LINK MADE!");
					savedTrigger = null;
				}
				actionNav = false;
				sb.cp5.show();
			}
			return;
		}
		if(linking) {
			if(linkHover != null) {
				sb.println("CLICK LINK " + linkHover);
				sb.println("RESPS - " + dgraph.length);
				DialogGNode gn = null;
				if(linkHover == dgraph.length) { // new action
					NPCAction a = npc.addAction();
					gn = new DialogGNode(a);
					DialogGNode[] dg2 = new DialogGNode[dgraph.length + 1];
					PApplet.arrayCopy(dgraph,0,dg2,0,dgraph.length);
					dg2[dgraph.length] = gn;
					dgraph = dg2;
				} else {
					gn = dgraph[linkHover];
				}
				dnode.setResponseTarget(linkR, gn);
				setVizOfNode(gn);
				linking = false;
				sb.cp5.show();
			}
			return;
		}
		if(savedTrigger != null) {
			actionNav = true;
		}
		if(hoverBody != null) {
			sb.println("HIT BODY");
			if(hoverBody == bodies[0]) {
				sb.println("HIT SAY");
				//go back, pop off the stack
				/**
				int elen = sb.editPath.length;
				if(elen != 0) { //already empty
					int[] newPath = new int[elen - 1];
					for(int i=0;i<elen-1;++i)
						newPath[i] = sb.editPath[i];
					DialogNode dn = dtree.getNode(newPath);
					if(dn != null) {
						sb.editPath = newPath;
						sb.dialogNode = new SBDialogNode(sb, dtree, newPath);
					}
				}	
				*/			
			}
			for(int i=1;i<responses.length+1;++i) {
				if(hoverBody == bodies[i]) {
					sb.println("HIT R BODY " + (i-1));
					//goto next node
					DialogGNode next = dnode.kids()[i-1];
					if(next != null)
						setVizOfNode(next);
				}
			}
			for(int i=responses.length+1;i<pars.length+responses.length+1;++i) {
				if(hoverBody == bodies[i]) {
					sb.println("HIT P BODY " + (i-responses.length - 1));
					//goto next node
					DialogGNode next = dnode.parents()[i-responses.length-1]._1();
					setVizOfNode(next);
				}
			}
		}
		
	}
	
	public void checkMouse() {
		hoverBody = null;
		for(int i=0;i<bodies.length;++i) {
			if(checkBod(bodies[i],sb.mouseX,sb.mouseY)) {
				hoverBody = bodies[i];
			}
		}
	}
	
	public void draw() {
		if(linking || actionNav || savedTrigger != null) {  //for choosing from the total list of actions - either navigation (actionNav) or linking (linking)
			
			//draw all the actions, give "new action" option too
			
			int numOpts = dgraph.length + 1;
			int rows = 5;
			int cols = 8;
			linkHover = null;
			for(int i=0;i<numOpts;++i) {
				int r = i/ cols;
				int c = i % cols;

				sb.fill(0);
				sb.stroke(255);
				sb.strokeWeight(3);
				
				if(sb.mouseX > c*100 && 
					sb.mouseX < (c+1)*100 &&
					sb.mouseY > r*100 &&
					sb.mouseY < (r+1)*100) {
					linkHover = i;
					sb.fill(40,40,40);
					sb.stroke(150,150,150);
				}
				sb.rect(c * 100,r * 100,100,100);
				String text = "New Action";
				if(i < numOpts-1) {
					text = dgraph[i].action().say();
				}
				text = formatStr(text, 15, 15);
				sb.fill(0,255,0);
				sb.textAlign(PConstants.LEFT);
				sb.textFont(sb.monoB10);
				sb.text(text,c * 100 + 5,r*100+10);
			}
			
			return;
		}

		
		//normal behaviour is to day the current action's SAY
		//along with its parent and kid responses
		//positions are held by the clickable bodies
		
		sb.textFont(sb.lucida10);
		sb.textAlign(PConstants.LEFT);
		
		NPCAction action = dnode.action();
		
		for(int i=0;i<bodies.length;++i) {
			float x = bodies[i].getPosition().getX();
			float y = bodies[i].getPosition().getY();
			if(hoverBody != null && hoverBody == bodies[i]) { //set selected style
				sb.stroke(255);
				sb.strokeWeight(3);
				sb.fill(155,30,30);
			} else { //unselected style
				sb.noStroke();	
				sb.fill(255,0,0);
			}
			sb.pushMatrix();
			sb.translate(x,y);
			sb.rect(-25,-25,50,50);
			sb.fill(0);
			String r = action.say();
			if(i > 0) {
				if(i<responses.length+1)
					r = responses[i-1];
				else
					r = pars[i-1-responses.length];
			}
			if(editInd != null && editInd == i) {
				sb.fill(0,255,0);
				r = editString;
			}
			sb.text(r,0,0);
			sb.popMatrix();
		}
	}
	
}