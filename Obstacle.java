import java.awt.*;

public class Obstacle {
    double x,y,radius;
    
    public Obstacle(double x, double y, double radius)
    {
        this.x=x;
        this.y=y;
        this.radius=radius;
    }
    
    
    public void draw(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillOval((int)(x-radius+.5),(int)(y-radius+.5),(int)(2*radius),(int)(2*radius));
    }
    
    
    public boolean shipCollision(Ship ship)
    {
        if(Math.pow(radius+ship.getRadius(),2) > Math.pow(ship.getX()-x,2)+Math.pow(ship.getY()-y,2) && ship.isActive())
            return true;
        return false;
    }
    
}
