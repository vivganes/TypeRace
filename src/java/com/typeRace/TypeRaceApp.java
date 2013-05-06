/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Messages received
 * ===========================================
 * 1. Player id request - PLAYER ? (first one gets 1 , second one gets 2 and all others get error)
 * 2. Type event - TYPE PLAYER1 C <chartyped>, TYPE PLAYER1 B (c for char, b for backspace)
 * 
 * 
 * Messages Sent
 * ==========================================
 * 1. Countdowns - COUNTDOWN 5, COUNTDOWN  4, COUNTDOWN 3, ETC.
 * 2. Score Update - SCORE PLAYER1 12, SCORE PLAYER2 11
 * 3. Player allocation (1 or 2) - PLAYER 1, PLAYER 2
 * 4. Gameover - END
 * 
 */
package com.typeRace;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

/**
 *
 * @author vivganes
 */
public class TypeRaceApp extends WebSocketApplication{
    
    private Player firstPlayer = null;
    private Player secondPlayer = null;
    private long startTime = -1;
    private boolean gameIsOn = true;

    @Override
    public boolean isApplicationRequest(HttpRequestPacket hrp) {
        return true;
    }
    
   @Override
   public void onMessage(WebSocket socket, String text) {
       if(gameIsOn){
       if(text.equals("PLAYER ?")){ //new player joined
           sendPlayerIdentification(socket);
       }
       
       else if(text.contains("TYPE")){ // player typed something
           String[] wordsInMessage = text.split(" ");
           handleTypeEvent(wordsInMessage[1], wordsInMessage[2]);
           sendScoreUpdate();
       }
       }      
   }
    
   private void handleTypeEvent(String playerName, String message){
       getPlayerWithPlayerName(playerName).charsTyped+=1;
       sendScoreUpdate();       
   }
   
   private Player getPlayerWithPlayerName(String name){
       if(firstPlayer.name.equals(name)){
           return firstPlayer;
       } else if (secondPlayer.name.equals(name)){
           return secondPlayer;
       }
       return null;
   }
   
   private void sendToAllSockets(String text){      
       //this should not be more than two at any point
        Set<WebSocket> connectedSockets = this.getWebSockets();
         for(WebSocket socket : connectedSockets){             
                socket.send(text);                                  
        }       
   }
   
   private void disconnectAllSockets(){
        Set<WebSocket> connectedSockets = this.getWebSockets();
         for(WebSocket socket : connectedSockets){             
                socket.close();                             
        }  
   }
   
    private void sendCountDown(){        
        for(int i =5; i>=1;i--){      
            try {
                sendToAllSockets("COUNTDOWN "+ i);
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TypeRaceApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sendToAllSockets("COUNTDOWN "+ 0);
        sendScoreUpdate();        
        startTime = System.currentTimeMillis();
        startClock();        
    }
    
    private void sendScoreUpdate(){
        long currentTime = System.currentTimeMillis();
        sendToAllSockets("SCORE PLAYER1 "+ (firstPlayer.charsTyped/((currentTime - startTime)/1000)));
        sendToAllSockets("SCORE PLAYER2 "+ (secondPlayer.charsTyped/((currentTime - startTime)/1000)));
    }
    
    private void sendPlayerIdentification(WebSocket socket){
        if(gameIsOn){
        if(firstPlayer == null){
            firstPlayer = createNewPlayer("PLAYER1");
            socket.send("PLAYER 1"); // YOU ARE PLAYER NUMBER 1
        }else{
            if(secondPlayer == null){
                secondPlayer = createNewPlayer("PLAYER2");
                socket.send("PLAYER 2"); //YOU ARE PLAYER NUMBER 2
                sendCountDown();
            }else{
                socket.send("PLAYER 0"); // NO PLACE TO PLAY 
                socket.close();
            }
        }
        }
    }
    
    private void startClock(){
       Thread clock = new Thread(){
           public void run(){               
               while(true){
                   if(System.currentTimeMillis() - startTime >= 3*60*1000){
                       gameIsOn = false;
                       sendScoreUpdate();
                       sendToAllSockets("END");    
                       endGame();
                       break;
                   }
                   
                   try {
                        Thread.currentThread().sleep(1000);
                        if(secondPlayer != null){
                        sendScoreUpdate();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TypeRaceApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
               }
           }
       };
       clock.start();       
    }
    
    private void endGame(){
        firstPlayer = null;
        secondPlayer = null;
        startTime = -1;
        gameIsOn = true;               
    }
    
    
    private Player createNewPlayer(String name){
        return new Player(name);
    }    
}

class Player{
    String name;
    double charsTyped=0;    
    
    Player(String name){
        this.name = name;
    }
}
