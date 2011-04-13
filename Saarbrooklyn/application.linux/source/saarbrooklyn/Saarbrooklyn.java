package saarbrooklyn;

import gifAnimation.Gif;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.Textarea;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.AABox;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.strategies.QuadSpaceStrategy;
import pointclick.GameState;
import pointclick.GameStateLoader;
import pointclick.NPC;
import pointclick.NPCAction;
import pointclick.Scene;
import pointclick.SceneJump;
import pointclick.SceneLoader;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import rita.RiTa;
import rita.RiTaEvent;
import scala.Tuple2;

/**
 * TODO
 * 
 * Conditions on responses
 * moving between screens
 * animation sequences
 * dialog graph
 * 
 * 
 * @author chonger
 *
 */

public class Saarbrooklyn extends PApplet {
	
	String sceneFilename = null;
	
	ControlP5 cp5;
	
	PImage backG,frontG; 
	PFont caps15,lucida10,mono30,type20,type30,mono20,monoB10;
	
	World world;

	//MAIN STRUCTURE OBJECTS
	Player p;
	Scene scene;
	float xOff,maxXOff;
	GameState gameState;
	NonPlayer[] npcs;
	
	
	boolean shifting = false;
	boolean alting = false;
	
	/**
	 * Boundary Editor variables
	 * 
	 */
	Vector2f newBound = null;
	boolean rectOrCircle = false;
	Body hoverBody = null;
	NonPlayer drag = null;
	NonPlayer tDrag = null;
	
	
	/**
	 * Game mode variables
	 * 
	 */
	boolean showBounds = false;
	
	boolean enteringAction = false; //are we forming an action chain?
	String savedActionCommand = ""; //saved action command
	Integer savedActionIndex; //saved action index (for switch statements)
	
	boolean dialogMode = false; //are we speaking? (the bottom blacked out, etc)
	NonPlayer currentActor = null;  //the npc with which we are interacting
	NPCAction currentAction = null; //the current npc action node we are at
	
	String status = null; //status bar text
	
	/**
	 * Dialog Editor variables
	 * 
	 */
	
	NonPlayer editorNPC = null;
	SBDialogNode dialogNode = null;
	
	/**
	 * These values are updated with each draw to detect hovering.  null means no hover.
	 */
	Integer hoverResponse = null; //hovering over response in game mode
	Integer actionHover = null;  //hovering over actions in game mode
	Integer inventHover = null;  //hovering over inventory in game mode
	NonPlayer npcHover = null; //hovering over an npc in game mode
	
	/**
	 * Static internal game strings
	 * 
	 * Made for German version.
	 * 
	 */
	
	String[] actions = {
			"Gehe zu",
			"Schau an",
			"Öffne",
			"Bewege",
			"Konsumiere",
			"Hebe auf",
			"Schließe",
			"Benutze",
			"Rede mit",
			"Pöbeln",
			"Schmeicheln",
			"Gib"	
		};
	
	String[] actionStatus = {
		"Wohin wilst du gehen?",
		"Was wilst du dir anschauen?",
		"Was wilst du Öffnen?",
		"Was wilst du Bewegen?",
		"Was wilst du Konsumieren?",
		"Was wilst du aufheben?",
		"Was wilst du Schließen?",
		"Was wilst du Benutzen?",
		"Mit wem wilst du reden?",
		"Wen wilst du anpöbeln?",
		"Wem wilst du schmeicheln?",
		"Was wilst du geben?",
		"Mit was wilst es benutzen?",
		"Wem wilst du es geben?"
	};
	
	/**
	 * Help menus
	 * 
	 */
	
	FadeNote fader = null;
	FadeNote gameHelp = new FadeNote("hold b to see boundaries\n" + 
		"SHIFT-B : Boundary Editor mode\nSHIFT-O while hovering over" + 
		" an NPC\nto enter Dialog editing mode",
		Color.black,
		Color.white,
		Color.red,
		false);
	FadeNote boundaryHelp = new FadeNote("x - new rectangle      c - new circle\n" + 
			"r - resize     d - delete\n\n" + 
			"SHIFT-G : Game Mode",
			Color.black,
			Color.white,
			Color.red,
			false);
	FadeNote objectHelp = new FadeNote("Help for dialog editor.",
			Color.black,
			Color.white,
			Color.red,
			false);
	
	/**
	 * 4 Game modes - game, boundary, perspective, and dialog editor
	 *
	 */
	
	enum Mode {
		GAME,
		BOUNDARY,
		DIALOG,
		PERSPECT
	}

	Mode mode;

	Integer hoverPerspect = null;
	Integer editPerspect = null;
	ControlFont cf;
	
	/**
	 * Check existence conditions for each npc and regenerate the list
	 * 
	 * this should be called whenever the game state changes
	 * 
	 */
	public void refreshNPCs() {
		ArrayList<NonPlayer> n = new ArrayList<NonPlayer>();
		for(int i=0;i<scene.npcs().length;++i) {
			if(scene.npcs()[i].exists(gameState))
				n.add(new NonPlayer(this,scene.npcs()[i]));
		}
		npcs = new NonPlayer[n.size()];
		npcs = n.toArray(npcs);
	}
	
	public void setup() {
		size(800,500);
		
		//File f = new File("data/scenes/Saarbrooklyn");
		//println("ABSPATH = " + f.getAbsolutePath());
		
		//println("USER DIRECTORY = " + System.getProperty("user.dir"));
		
		gameState = new GameStateLoader().fromXML("data/scenes/Saarbrooklyn.xml",this);	
			
		world = new World(new Vector2f(0,0), 10, new QuadSpaceStrategy(100,15));
		
		p = new Player(this,300,280,"sam");
		
		loadScene(gameState.firstScene());
		addSceneJumps();
		
		p.dest = (Vector2f) p.bod.getPosition();

		mode = Mode.GAME;
		
		caps15 = loadFont("Caps15.vlw");
		lucida10 = loadFont("Lucida10.vlw");
		mono30 = loadFont("Mono30.vlw");
		type20 = loadFont("Type20.vlw");
		type30 = loadFont("Type30.vlw");
		mono20 = loadFont("Mono20.vlw");
		monoB10 = loadFont("MonoB10.vlw");		
		println("Loaded fontz");
		cp5 = new ControlP5(this);
		//cf = new ControlFont(createFont("Times",12));
		cf = new ControlFont(createFont("Times",12));
		cp5.setControlFont(cf);
	}
	
	/**
	 * Load a scene.  The destination of the player should be set
	 * by the setup or else by the screen we're coming from.  
	 * The scene jumps are setup as soon as the player reaches his first 
	 * destination.
	 * 
	 * @param sc
	 */
	public void loadScene(String sc) {
		
		world.clear();
		sceneFilename = gameState.getScene(sc);
		
		world.add(p.bod);
		scene = new SceneLoader().fromXML(gameState.getScene(sc),this);
		backG = loadImage(scene.behind());
		
		xOff = 0;
		maxXOff = backG.width - width;
		
		if(scene.infront().length() > 0)
			frontG = loadImage(scene.infront());
		else
			frontG = null;
		refreshNPCs();
		
		Body[] bs = scene.getBoundaries();
		for(Body b : bs) {
			world.add(b);
		}
		
	}
	
	public void onRiTaEvent(RiTaEvent re) {
		if(re.getName().equals("ENDCONVO")) { //End a conversation
			endConversation();
		}
		if(re.getName().equals("PLAYERSPEAK")) {
			pDoneSpeak();
		}
		if(re.getName().equals("NPCANIM")) {
			advanceNPCAnim();
		}
	}
	
	//Pass any controlp5 to the dialog editor
	public void controlEvent(ControlEvent theEvent) {
		if(dialogNode != null) {
			dialogNode.handleCP5(theEvent);
		}
	}

	public void endConversation() {
		if(currentActor != null)
			setCurrentNPCImg(currentActor.npc.restImg());
		currentActor = null;
		currentAction = null;
		dialogMode = false;
	}
	
	//advance the image, return to rest image if the animation is over
	public void advanceNPCAnim() {
		if(currentActor.animIndex + 1 == currentAction.animation().length)
			currentActor.npc.setImgInd(currentActor.npc.restImg());
		else {
			currentActor.animIndex += 1;
			int ind = currentAction.animation()[currentActor.animIndex]._1();
			float dur = currentAction.animation()[currentActor.animIndex]._2();
			setCurrentNPCImg(ind);
			RiTa.setCallbackTimer(this, "NPCANIM", dur,false);
		}
	}
	
	public void pDoneSpeak() { //player is done speaking
		p.talk = false;
		if(currentAction == null) { //if there is no next action
			endConversation();
		} else { //do the next action
			executeAction();
		}
	}
	
	public void executeAction() {
		currentAction.doEffect(gameState);
		refreshNPCs(); //the effect might introduce or remove NPCs
		//find the current actor again by comparing npc pointers
		for(int i=0;i<npcs.length;++i) {
			if(npcs[i].npc == currentActor.npc) {
				currentActor = npcs[i];
			}
		}
		if(currentAction.responses().length == 0) { //end the conversation where the npc has the last word
			println("NPC Ends conversation");
			RiTa.setCallbackTimer(this, "ENDCONVO",3,false);
		}
		currentActor.animIndex = -1;
		advanceNPCAnim();
	}
	
	/**
	 * Set a new image for the current NPC, and loop its gif if it is a gif
	 * 
	 * @param index
	 */
	public void setCurrentNPCImg(int index) {
		currentActor.npc.setImgInd(index);
		if(currentActor.imgs[index] instanceof Gif) {
			println("loopin a gif");
			((Gif) currentActor.imgs[index]).loop();		
		}
	}

	/**
	 * Should get called with an action command that is complete
	 * 
	 * 
	 * @param npc
	 * @param trigger
	 */
	public void triggerNPC(NonPlayer npc, String trigger) {
		enteringAction = false;
		p.destNPC = null;
		currentActor = npc;
		println("TRIGGER - " + trigger);
		println(currentAction);
		NPCAction actn = currentActor.npc.getAction(trigger,gameState); //the action will be null if there is no such trigger
		if(actn != null) { //if there is a trigger with this name stored in the NPC
			println("Trigger found");
			currentAction = actn;
			dialogMode = true;
			executeAction();		
		} else {
			println("Using default can't do it phrase");
			currentActor = null;
			//default behaviour
			switch(savedActionIndex) {
			case 0: break; //walk to, cannot fail
			case 1: justSay(npc.npc.description(),3); break; //look at
			case 2: justSay("Ich weiss nicht wie Ich das öffnen soll.",3); break; //open
			case 3: justSay("Ich kann das nicht bewegen",3);break; //move
			case 4: justSay("Ich will das nicht essen",3); break; //consume
			case 5: justSay("Das ist zu schwer ",3); break; //pick up
			case 6: justSay("Ich kann das nicht schließen",3); break; //close
			case 7: justSay("Ich weiss nicht wie",3); break; //use, with single
			case 8: justSay("Es sieht nicht zu gespraUchig aus.",3); break; //talk to
			case 9: justSay("Das ist gemein.",3); break; //harass
			case 10: justSay("Sie verdienen es nicht",3); break; //praise
			case 11: justSay("ERROR",3); break; //give, only works from inventory
			case 12: justSay("Ich weiss nicht, wie Ich das benutzen soll.",3); break; //use2
			case 13: justSay("Ich glaube ich behalt es",3); break; //give2			
			}
		}
	}
	
	public void justSay(String s,float dur) {		
		p.talk = true;
		p.words = s;
		//TODO : Player animations!  The player doesnt talk yet.
		RiTa.setCallbackTimer(this,"PLAYERSPEAK",dur,false);
	}
	
	public void triggerWalk(NonPlayer npc, String trigger) {
		println("Trigger Walk :" + trigger);
		Vector2f tDest = new Vector2f(npc.npc.x() + npc.npc.tx(), 
				npc.npc.y() + npc.npc.ty()); //go to the target spot for this npc
		p.setDestination(tDest);
		p.destNPC = npc;
		p.savedAction = trigger;
	}
	
	public void mouseReleased() {
		switch(mode) {
		case GAME:
			break;
		case BOUNDARY:
			break;
		case DIALOG:
			break;
		}
	}
	
	public void mouseClicked() {
		println("MX: " + mouseX + " MY: " + mouseY);
		switch(mode) {
		case GAME: 
			if(p.cutControl) //controls are cur when entering a scene
				return;
			if(p.talk) { //if the player is talking, the controls are cut too
				println("Player talking, no action taken");
				return;
			}
			if(mouseY < 340 && !dialogMode && !enteringAction) { //basic game click
				if(npcHover != null) { //if we're over an npc
					savedActionIndex = 0; 
					triggerWalk(npcHover,"Gehe Zu"); //trigger Walk To action
				} else { //if no hovering NPC, just move
					p.destNPC = null;
					p.setDestination(new Vector2f(mouseX+xOff,mouseY));
				}
			} else if(dialogMode && hoverResponse != null && currentAction != null) { //clicking on a response
				
				//the npcAction check is because 
				
				p.talk = true;
				p.words = currentAction.responses()[hoverResponse]._1();
				currentAction = currentAction.responses()[hoverResponse]._2();
				if(p.words.length() > 0)
					RiTa.setCallbackTimer(this, "PLAYERSPEAK",3,false);
				else
					pDoneSpeak();
			} else if(mouseY > 340 && !dialogMode && actionHover != null) { //start an action chain
				enteringAction = true;
				String actn = actions[actionHover];
				p.savedAction = actn;
				savedActionCommand = actionStatus[actionHover];
				savedActionIndex = actionHover;
			} else if(enteringAction) { //in the middle of clicking a full action
				if(mouseY < 340) { //if we're in the game screen
					if(npcHover == null) { //and click empty space
						enteringAction = false; //we're done entering 
						p.destNPC = null; //null destination
						p.setDestination(new Vector2f(mouseX,mouseY)); //go to the new location 
					} else { //if we're over an npc
						
						//the action is either give or use, and we only want to trigger
						//if the command has already picked a inventory item
						
						if(p.savedAction.equals("Gib")) {
							justSay("Das gehort nicht mir.",3);
						} else {							
							enteringAction = false;
							triggerWalk(npcHover,p.savedAction);
						}
					}
				}
				if(mouseY > 340 && !p.talk) {
					if(actionHover != null) {
						String actn = actions[actionHover];
						p.savedAction = actn;
						savedActionCommand = actionStatus[actionHover];
						savedActionIndex = actionHover;
					} else if (inventHover != null) {
						//do something with the item
						switch(savedActionIndex) {
						case 11: //give
							savedActionIndex = 13; //to whom
							savedActionCommand = actionStatus[savedActionIndex];
							p.savedAction += "," + gameState.getInv(inventHover);
							break;
						case 7: //use
							savedActionIndex = 12; //with what
							savedActionCommand = actionStatus[savedActionIndex];
							p.savedAction += "," + gameState.getInv(inventHover);
							break;
						case 12: //finish a use
							savedActionIndex = 12; //with what
							String i2 = gameState.getInv(inventHover);
							String i1 = p.savedAction.split(",")[1];
							println("COMB " + i1 + " and " + i2);
							String x = gameState.combine(i1, i2);
							if(x == null) {
								justSay("That doesn't work.",2);
							} 
							break;
						default:
							enteringAction = false;
						}
					} else {
						enteringAction = false;
					}
				}
				
			}
			break;
		case BOUNDARY:
			if(newBound != null) {
				if(rectOrCircle) {
					float x = Math.min(mouseX+xOff,newBound.x);
					float y = Math.min(mouseY,newBound.y);
					float w = Math.max(mouseX+xOff,newBound.x) - x;
					float h = Math.max(mouseY,newBound.y) - y;
					if(w > 0 && h > 0) {
						Body b = new StaticBody(new Box(w,h));
						float w2 = ((Box) b.getShape()).getSize().getX();
						float h2 = ((Box) b.getShape()).getSize().getY();
						println("W/H!" + w + "," + h);
						println("W/H " + w2 + "," + h2);
						b.setPosition(x+w/2,y+h/2);
						world.add(b);
						scene.addBoundary(b);
						newBound = null;
					}
				} else {
					float x = newBound.x;
					float y = newBound.y;
					float r = new Vector2f(x,y).distance(new Vector2f(mouseX+xOff,mouseY));
					if(r > 0) {
						Body b = new StaticBody(new Circle(r));
						b.setPosition(x,y);
						world.add(b);
						scene.addBoundary(b);
						newBound = null;
					}
				}
			} else { //not creating a new boundary
				println("NORMAL LCICK");
				if(drag != null) {
					drag = null;
				} else if(tDrag != null) {
					tDrag = null;
				} else {
					boolean found = false;
					Vector2f m = new Vector2f(mouseX+xOff, mouseY);
					for(NonPlayer n : npcs) {
						if(m.distance(new Vector2f(n.npc.x() + n.npc.tx(),n.npc.y() + n.npc.ty())) < 10) {
							found = true;
							tDrag = n;
						}
					}
					if(found) {
						
					} else if(npcHover != null) {
						println("SET DRAG");
						drag = npcHover;
					}
				}
			}
			break;
		case DIALOG: //clicking in the object editor
			dialogNode.click();
			break;
		case PERSPECT:
			if(editPerspect != null) {
				editPerspect = null;
			} else if(hoverPerspect != null) {
				editPerspect = hoverPerspect;
			}
			break;
		}
	
	}
	
	public void keyReleased() {
		if(keyCode == SHIFT) {
			shifting = false;
		}
		if(keyCode == ALT) {
			alting = false;
		}
		if(key == 'h') {
			fader = null;
		}
		switch(mode) {
		case GAME: 
			if(key == 'b')
				showBounds = false;
			break;
		case BOUNDARY:break;
		case DIALOG:break;
		
		}
		

	}
	
	public void modeSwitch() {
		//println("KEY " + key + " PRESSED AND shifting = " + shifting);
		
		if(shifting) { 
			if(key == 'B') {
				fader = new FadeNote("Boundary Editor Mode",Color.green,Color.orange,
						Color.black,true);
				mode = Mode.BOUNDARY;
				if(cp5 != null)
					cp5.hide();
			}
			if(key == 'P') {
				fader = new FadeNote("Perspective Editor Mode",Color.green,Color.orange,
						Color.black,true);
				mode = Mode.PERSPECT;
				if(cp5 != null)
					cp5.hide();
			}
			if(key == 'G') {
				fader = new FadeNote("Game Mode",Color.black,Color.pink,
						Color.cyan,true);
				mode = Mode.GAME;
				if(cp5 != null)
					cp5.hide();
			}
			if(key == 'S') {
				println("SAVING");
				fader = new FadeNote("Saving to " + sceneFilename,Color.green,Color.orange,
						Color.white,true);
				scene.save(sceneFilename);
			}
			if(key == 'O') {
				if(npcHover != null) {
					fader = new FadeNote("NPC Editor Mode",Color.black,Color.pink,
							Color.cyan,true);
					mode = Mode.DIALOG;
					editorNPC = npcHover;
					
					dialogNode = new SBDialogNode(this,editorNPC.npc,editorNPC);
				} else {
					fader = new FadeNote("Please put the mouse over an \nNPC to enter editing mode",Color.black,Color.pink,
							Color.cyan,true);
				}
			}
		}
	}
	
	public void keyPressed() {
		
		if(keyCode == SHIFT) {
			shifting = true;
		}
		if(keyCode == ALT) {
			alting = true;
		}
		
		switch(mode) {
		case GAME:
			if(key == 'h')
				fader = gameHelp;
			if(key == 'b')
				showBounds = true;
			modeSwitch();
			break;
		case BOUNDARY:
			if(tDrag != null) {
				if(keyCode == UP) {
					tDrag.npc.setFace(0);
				}
				if(keyCode == DOWN) {
					tDrag.npc.setFace(1);				
				}
				if(keyCode == LEFT) {
					tDrag.npc.setFace(3);
				}
				if(keyCode == RIGHT) {
					tDrag.npc.setFace(2);	
				}
			}
			if(key == 'h')
				fader = boundaryHelp;
			if(key == 'c') {
				newBound = new Vector2f(mouseX+xOff, mouseY);
				rectOrCircle = false;
			} 
			if(key == 'x') {
				newBound = new Vector2f(mouseX+xOff, mouseY);
				rectOrCircle = true;
			}
			if(key == 'd') { //if there is a bound under X, then delete it
				if(hoverBody != null) {
					world.remove(hoverBody);
					scene.remBoundary(hoverBody);
				}
			}
			if(key == 'r') { //if there is a bound under the mouse, start to resize it
				if(hoverBody != null) {
					rectOrCircle = isBox(hoverBody);
					if(isBox(hoverBody)) {
						float x = hoverBody.getPosition().getX();
						float y = hoverBody.getPosition().getY();
						float w = ((Box) hoverBody.getShape()).getSize().getX();
						float h = ((Box) hoverBody.getShape()).getSize().getY();
						newBound = new Vector2f(x-w/2,y-h/2);
					} else {
						float x = hoverBody.getPosition().getX();
						float y = hoverBody.getPosition().getY();
						newBound = new Vector2f(x,y);
					}
					world.remove(hoverBody);
					scene.remBoundary(hoverBody);
					
				}	
			}
			modeSwitch();
			break;
		case DIALOG:
			if(dialogNode.addSomething) { //only deal with cp5 commands
				
			} else if(dialogNode.editInd == null) {
				modeSwitch();
				if(key == ' ') {
					dialogNode.startEdit();
				}
				if(key == 'h')
					fader = objectHelp;
				if(key == 'd') {
					dialogNode.remove();
				}
				if(key == 'l') {
					dialogNode.link();
				}
				if(key == 'n') {
					dialogNode.addResponse();
				}
				if(key == 'c') {
					dialogNode.actionNav = true;
					cp5.hide();
				}
			
			} else {
				if(keyCode == ENTER)
					dialogNode.endEdit();
				
				dialogNode.handleKey(key);
			}			
			break;
		case PERSPECT:
			modeSwitch();
			if(editPerspect != null) {
				if(keyCode == UP) {
					float newS = Math.min(2.0f, scene.perspects()[editPerspect]._2() + .1f);
					scene.setPerspective(editPerspect,newS);
				}
				if(keyCode == DOWN) {
					float newS = Math.max(.3f, scene.perspects()[editPerspect]._2() - .1f);
					scene.setPerspective(editPerspect,newS);				
				}
			} else {
				if(key == 'n') {
					scene.addPerspective(mouseX,mouseY,1f);
				}
				if(key == 'd' && hoverPerspect != null) {
					scene.remPerspective(hoverPerspect);
				}
			}
			break;
		}
	}
	
	public boolean isBox(Body b) {
		return b.getShape() instanceof Box;
	}
	
	public void checkNPCHover() {
		npcHover = null;
		//println("NPCS - " + npcs.length);
		for(NonPlayer npc : npcs) {
			float x = npc.npc.x()-xOff;
			float y = npc.npc.y();
			float w = npc.npc.w();
			float h = npc.npc.h();
			if(mouseX > x-w/2 &&
					mouseX < x + w/2 &&
					mouseY > y - h/2 &&
					mouseY < y + h/2) {
				npcHover = npc;
				status = npc.npc.name();
			}
		}
	}
	
	public void addSceneJumps() {
		SceneJump[] sj = scene.sceneJumps();
		for(SceneJump b : sj) {
			world.add(b.triggerBody());
		}
	}
	
	public void draw() {
		if(enteringAction)
			status = savedActionCommand;
		else
			status = null;
		switch(mode) {
		case GAME: 
			
			checkNPCHover();
			
			pushMatrix();
			translate(-xOff,0);
			image(backG,0,0);
			if(showBounds)
				drawBoundaries();
			p.update();
			//println("PLAYER - " + p.bod.getPosition().getX() + " - " + p.bod.getPosition().getY());
			for(NonPlayer npc : npcs) {
				if(npc.npc.bod().getPosition().getY() <= p.bod.getPosition().getY()) {
					npc.draw();
				}
			}
			p.draw();
			for(NonPlayer npc : npcs) {
				if(npc.npc.bod().getPosition().getY() > p.bod.getPosition().getY()) {
					npc.draw();
				}
			}
			if(frontG != null)
				image(frontG,0,0);
			popMatrix(); //DONE DRAWING Game screen
			if(currentActor != null && !p.talk) {
				textFont(lucida10);
				textAlign(CENTER);
				fill(255);
				text(currentAction.say(),currentActor.npc.x()-xOff,currentActor.npc.y() - currentActor.npc.h()/2 - 20);
			}
			
			if(p.talk) {
				textFont(lucida10);
				textAlign(CENTER);
				fill(255);
				text(p.words,p.bod.getPosition().getX()-xOff,p.bod.getPosition().getY() - 80);
			}
			
			float xx = p.bod.getPosition().getX();
			float yy = p.bod.getPosition().getY();
			if(xx-xOff > 600) {
				xOff = Math.min(maxXOff, xOff + 2);
			}
			if(xx-xOff < 200) {
				xOff = Math.max(0, xOff - 2);
			}
			
			//game screen drawn, now drawing controls
			
			if(!dialogMode) {
				
				noStroke();		
				fill(0,50,10);
				rect(0,370,800,130);
				
				//DRAW ACTIONS
				
				textFont(type30);
				textAlign(LEFT);
				
				int[] xoff = {5,125,270,420};
				textSize(25);
				actionHover = null;
				fill(0,128,128);
				for(int i=0;i<3;++i) {
					for(int j=0;j<4;++j) {
						int ind = i * 4 + j;
						String s = actions[ind];
						float ay = 400 + 30 * j;
						if(mouseX > xoff[i] &&
								mouseY > ay-26 &&
								mouseX < xoff[i+1] &&
								mouseY < ay + 4) {
							actionHover = new Integer(ind);
							if(!enteringAction)
								status = s;
							fill(100,228,228);
							text(s,xoff[i],ay);
							fill(0,128,128);
						} else {
							text(s,xoff[i],ay);
						}
					}
				}
				
				//DRAW INVENTORY
		
				inventHover = null;
				fill(255);
				textFont(caps15);
				textAlign(CENTER);
				text("Inventar",590,390);
				
				fill(0,10,50);
				stroke(0);
				strokeWeight(2);
				float sX = 430;
				float sY = 395;
				float invw = 320;
				float invh = 96;
				float xW = invw / 32;
				float yH = invh / 32;
				rect(sX,sY,invw,invh);
				for(int i=0;i<gameState.inventory().size();++i) {
					float mx = sX + 32 * (i%10);
					float my = sY + 32 * (i/10);
					/**fill(255);
					stroke(0);
					strokeWeight(1);
					rect(mx,my,invw/3,invh/5);*/
					if(mouseX > mx &&
							mouseY > my &&
							mouseX < mx + 32 &&
							mouseY < my + 32) {
						inventHover = new Integer(i);
						status = gameState.getInv(i);
						fill(0,128,128);
						noStroke();
						rect(mx,my,32,32);
					} 
					/**
					textFont(type20);
					textAlign(CENTER);
					text(gameState.getInv(i),mx + invw/6,my + invh/10 + 6);
					*/
					image(gameState.getInvArt(i),mx,my);
				}
				
				//DRAW STATUS
				
				noStroke();		
				fill(0);
				rect(0,350,800,20);
				
				if(status != null) {
					fill(255);
					textFont(caps15);
					textAlign(CENTER);
					text(status,width/2,364);
				}
			} else {
				noStroke();		
				fill(0);
				rect(0,370,800,130);
				hoverResponse = null;
				if(currentAction != null && !p.talk) {
					int nresponses = currentAction.responses().length;
					float sy = 390;
					for(int i=0;i<nresponses;++i) {
						String t = currentAction.responses()[i]._1();
						
						textAlign(LEFT);
						textFont(type20);
						float y = sy + i*20;
						if(mouseY > y - 18 &&
							mouseY < y + 2) {
							hoverResponse = new Integer(i);
							fill(128,128,0);
						} else {
							fill(0,128,128);
						}
						text(t,10,sy + i * 20);
						
					}
				}
			}
			if(p.cutControl && p.dest == null && p.jump != null) { //take the jump
				println("TAKE JUMP");
				SceneJump s = p.jump;
				p.jump = null;
				p.bod.setPosition(s.arriveS().x,s.arriveS().y);
				p.dest = s.arriveE();
				loadScene(s.targetScene());
			}
			if(p.cutControl && p.dest == null && p.jump == null) { //give control back
				println("GIVE CONTROl");
				p.cutControl = false;
				
				addSceneJumps();

			}
			break;
		case BOUNDARY:
			
			//Update the NPC hover detector
			npcHover = null;
			for(NonPlayer npc : npcs) {
				float x = npc.npc.x();
				float y = npc.npc.y();
				float w = npc.npc.w();
				float h = npc.npc.h();
				if(mouseX+xOff > x-w/2 &&
						mouseX+xOff < x + w/2 &&
						mouseY > y - h/2 &&
						mouseY < y + h/2) {
					npcHover = npc;
					status = npc.npc.name();
				}
			}
			
			//do npc drag
			if(drag != null) {
				drag.npc.setPos(mouseX+xOff, mouseY);
			}
			//do talk drag
			if(tDrag != null) {
				tDrag.npc.setTalkXY(mouseX+xOff, mouseY);
			}
			hoverBody = scene.hoverOver(mouseX+xOff, mouseY); //find if we're hovering over a boundary

			pushMatrix();
			translate(-xOff,0);
			image(backG,0,0);
			drawBoundaries();
			if(frontG != null)
				image(frontG,0,0);
			popMatrix();
			
			
			fill(0);
			rect(0,340,800,160);
			
			if(newBound != null) {
				fill(0,255,0);
				stroke(0,0,255);
				strokeWeight(1);
				if(rectOrCircle) {
					float x = Math.min(mouseX,newBound.x-xOff);
					float y = Math.min(mouseY,newBound.y);
					float w = Math.max(mouseX,newBound.x-xOff) - x;
					float h = Math.max(mouseY,newBound.y) - y;
					rect(x,y,w,h);
				} else {
					float x = newBound.x;
					float y = newBound.y;
					float r = new Vector2f(mouseX+xOff,mouseY).distance(newBound);
					ellipse(x-xOff,y,2*r,2*r);
				}
			}
			pushMatrix();
			translate(-xOff,0);
			showBounds = true;
			for(NonPlayer npc : npcs) {
				npc.draw();
			}
			popMatrix();
			showBounds = false;
			break;
		case DIALOG:
			background(0,138,98);
			dialogNode.checkMouse();
			dialogNode.draw();
			
			break;
		case PERSPECT:
			pushMatrix();
			translate(-xOff,0);
			image(backG,0,0);
			if(frontG != null)
				image(frontG,0,0);
			popMatrix();
			hoverPerspect = null;
			int ind = -1;
			if(editPerspect != null) {
				scene.setPerspectiveXY(editPerspect, mouseX+xOff, mouseY);
			}
			for(Tuple2<Vector2f,Float> t : scene.perspects()) {
				ind += 1;
				Vector2f v = t._1();
				float rad = t._2() * 50;
				if(v.distance(new Vector2f(mouseX+xOff,mouseY)) < rad) {
					fill(0,158,208);
					stroke(200);
					strokeWeight(1);
					hoverPerspect = ind; 
				} else {
					fill(0,128,188);
					stroke(0);
					strokeWeight(2);
				}
				ellipse(v.x-xOff,v.y,rad,rad);
				
			}
			
			for(int i=0;i<10;++i) {
				for(int j=0;j<6;++j) {
					pushMatrix();
					translate(i*width/10,j*height/6);
					scale(scene.getScale(i*width/10 + xOff, j*height/6));
					image(p.stand[0],0,0);
					popMatrix();
				}
			}
			fill(0);
			rect(0,340,800,160);
			break;
		}
		
		//DRAW INFO
		pushStyle();
		if(fader != null) {
			if(fader.draw(this)) {
				fader = null;
			}
		}
		popStyle();
	}
	
	public void drawBoundaries() {
		Body[] bs = scene.getBoundaries();
		for(Body b : bs) {
			if(b != hoverBody) {
				fill(155,0,0);
				stroke(155);
				strokeWeight(2);
			} else {
				fill(255,0,0);
				stroke(255);
				strokeWeight(4);
			}
			if(isBox(b)) {
				float x = b.getPosition().getX();
				float y = b.getPosition().getY();
				float w = ((Box) b.getShape()).getSize().getX();
				float h = ((Box) b.getShape()).getSize().getY();
				rect(x-w/2,y-h/2,w,h);
			} else {
				float x = b.getPosition().getX();
				float y = b.getPosition().getY();
				float r = ((Circle) b.getShape()).getRadius();
				ellipse(x,y,2*r,2*r);
			}
		}
	}
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { saarbrooklyn.Saarbrooklyn.class.getName() });
	}
}
