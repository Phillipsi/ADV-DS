//Josh Holden
//Graphics-Asg1: Bouncing Shapes

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Formatter;
import java.util.*;
import javax.swing.*;

public class IndoorSoccerSimulator_SiHoldLee {
   public static void main(String[] args) {
      //int n = 11; //Integer.parseInt(JOptionPane.showInputDialog("How many balls do you want to put in motion?"));
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            JFrame frame = new JFrame("Soccer Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new SoccerGame(2000, 2600));
            frame.pack();// Preferred size of SoccerGame
            frame.setVisible(true);
         }
      });
   }
}

/**
 * A rectangular container box, containing the bouncing ball.  
 */
class ContainerBox {
   int minX, maxX, minY, maxY; // Box's bound
   private Color colorFilled; // Box's background color
   private Color colorBorder; // Box's border color
   
   /** Constructors */
   public ContainerBox(int x, int y, int width, int height, Color colorFilled, Color colorBorder) {
      minX = x;
      minY = y;
      maxX = x + width - 1;
      maxY = y + height - 1;
      this.colorFilled = colorFilled;
      this.colorBorder = colorBorder;
   }
   
   /** Set or reset the boundaries of the box. */
   public void set(int x, int y, int width, int height) {
      minX = x;
      minY = y;
      maxX = x + width - 1;
      maxY = y + height - 1;
   }

   /** Draw itself using the given graphic context. */
   public void draw(Graphics g) {
      g.setColor(colorFilled);
      g.fillRect(minX, minY, maxX - minX - 1, maxY - minY - 1);
      g.setColor(colorBorder);
      g.drawRect(minX, minY, maxX - minX - 1, maxY - minY - 1);
   }
}

class SoccerGame extends JPanel {
   private static final int UPDATE_RATE = 30;  // Frames per second (fps)
   
   /**
      0 index contains the goalie
      1-3 index contains defenders
      
   */
   private ArrayList<Person> team1;
   private ArrayList<Person> team2;
   
   private ContainerBox box;  // The container rectangular box
  
   private DrawCanvas canvas; // Custom canvas for drawing the box/ball
   private int canvasWidth;
   private int canvasHeight;
  
   /**
    * Set the drawing canvas to fill the screen (given its width and height).
    * Initializes balls
    * @param width : screen width
    * @param height : screen height
    */
   public SoccerGame(int width, int height) {
  
      canvasWidth = width;
      canvasHeight = height;
      balls = new ArrayList(0);
      // Initialize the ball at a random location (inside the box) and moveAngle
      for(int i = 0; i < numberOfPersons; i++){
         Random rand = new Random();
         int radius = 60;
         
         int x = rand.nextInt(canvasWidth - radius * 2 - 20) + radius + 10;
         int y = rand.nextInt(canvasHeight - radius * 2 - 20) + radius + 10;
         int speed = 3; //(int)(Math.random()*10+3);
         int angleInDegree = rand.nextInt(360);
         balls.add(new Person(x, y, radius, speed, angleInDegree, Color.BLUE));
      }
     
      // Initialize the Container Box to fill the screen
      box = new ContainerBox(0, 0, canvasWidth, canvasHeight, Color.LIGHT_GRAY, Color.WHITE);
     
      // Initialize the custom drawing panel for drawing the game
      canvas = new DrawCanvas();
      this.setLayout(new BorderLayout());
      this.add(canvas, BorderLayout.CENTER);
      
      // Handling window resize.
      this.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            Component c = (Component)e.getSource();
            Dimension dim = c.getSize();
            canvasWidth = dim.width;
            canvasHeight = dim.height;
            // Adjust the bounds of the container to fill the window
            box.set(0, 0, canvasWidth, canvasHeight);
         }
      });
  
      // Start the ball bouncing
      gameStart();
   }
   
   /** Start the ball bouncing. */
   public void gameStart() {
      // Run the game logic in its own thread.
      Thread gameThread = new Thread() {
         public void run() {
            while (true) {
               // Execute one time-step for the game 
               gameUpdate();
               // Refresh the display
               repaint();
               // Delay and give other thread a chance
               try {
                  Thread.sleep(1000 / UPDATE_RATE);
               } catch (InterruptedException ex) {}
            }
         }
      };
      gameThread.start();  // Invoke GaemThread.run()
   }
   
   /** 
    * One game time-step. 
    * Update the game objects, with proper collision detection and response.
    */
   public void gameUpdate() {
      for (int a = 0; a < balls.size(); a++){
         for (int c = a+1; c < balls.size(); c++){
            if (Math.pow(balls.get(a).getX() - balls.get(c).getX(), 2) + Math.pow(balls.get(a).getY() - balls.get(c).getY(), 2) <= Math.pow(balls.get(a).getR()+balls.get(c).getR(), 2)){
               balls.remove(c);
            }
         }
         balls.get(a).move(box);
      }
   }
   
   /** The custom drawing panel for the bouncing ball (inner class). */
   class DrawCanvas extends JPanel {
      /** Custom drawing codes */
      @Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g);    // Paint background
         // Draw the box and the ball
         box.draw(g);
         for (int b = 0; b < balls.size(); b++) {
            balls.get(b).draw(g);
         }
      }
  
      /** Called back to get the preferred size of the component. */
      @Override
      public Dimension getPreferredSize() {
         return (new Dimension(canvasWidth, canvasHeight));
      }
   }
}


class Person {
   double x, y;           // Person's center x and y (package access)
   double speedX, speedY; // Person's speed per step in x and y (package access)
   double radius;         // Person's radius (package access)
   private Color color;  // Person's color
  
   /**
    * Constructor: For user friendliness, user specifies velocity in speed and
    * moveAngle in usual Cartesian coordinates. Need to convert to speedX and
    * speedY in Java graphics coordinates for ease of operation.
    */
   public Person(double x, double y, double radius, double speed, double angleInDegree,
         Color color) {
      this.x = x;
      this.y = y;
      // Convert (speed, angle) to (x, y), with y-axis inverted
      this.speedX = (double)(speed * Math.cos(Math.toRadians(angleInDegree)));
      this.speedY = (double)(-speed * (double)Math.sin(Math.toRadians(angleInDegree)));
      this.radius = radius;
      this.color = color;
   }

   /** Draw itself using the given graphics context. */
   public void draw(Graphics g) {
      g.setColor(color);
      g.fillOval((int)(x - radius), (int)(y - radius), (int)(2 * radius), (int)(2 * radius));
   }
   public double getX() {return x;}
   public double getY() {return y;}
   public double getR() {return radius;}
   /** 
    * Make one move, check for collision and react accordingly if collision occurs.
    * 
    * @param box: the container (obstacle) for this ball. 
    */
   public void move(ContainerBox box) {
      // Get the ball's bounds, offset by the radius of the ball
      double ballMinX = box.minX + radius;
      double ballMinY = box.minY + radius;
      double ballMaxX = box.maxX - radius;
      double ballMaxY = box.maxY - radius;
   
      // Calculate the ball's new position
      x += speedX;
      y += speedY;
      // Check if the ball moves over the bounds. If so, adjust the position and speed.
      if (x < ballMinX) {
         speedX = -speedX; // Reflect along normal
         x = ballMinX;     // Re-position the ball at the edge
      } else if (x > ballMaxX) {
         speedX = -speedX;
         x = ballMaxX;
      }
      // May cross both x and y bounds
      if (y < ballMinY) {
         speedY = -speedY;
         y = ballMinY;
      } else if (y > ballMaxY) {
         speedY = -speedY;
         y = ballMaxY;
      }  
   }

}

class Person {
   private double x, y;
   private double speedX, speedY;
   private double radius;
   private Color color;
   private double speedFactor;
   private double angle;
   
}

class Goalie {

}
class Defender {

}
class Midfielder {

}
class Forward {

}