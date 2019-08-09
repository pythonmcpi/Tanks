package tanks.tank;

import java.util.ArrayList;

import tanks.Bullet;
import tanks.Game;
import tanks.event.EventCreateTank;

public class TankBoss extends TankAIControlled
{
	public ArrayList<Tank> spawned = new ArrayList<Tank>();
	
	public TankBoss(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size * 3, 255, 0, 0, angle, ShootAI.alternate);

		this.enableMovement = true;
		this.enableMineLaying = false;
		this.liveBulletMax = 4;
		this.cooldownRandom = 200;
		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;
		this.bulletBounces = 3;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletSpeed = 25.0 / 4;
		this.bulletSize = 25;
		this.bulletHeavy = true;
		this.lives = 5;
		this.baseLives = 5;
		this.turret.length *= 3;
		this.turret.size *= 3;
		this.coinValue = 25;
	}
	
	@Override
	public void update()
	{
		if (this.age <= 0)
		{
			for (int i = 0; i < 4; i++)
			{
				this.spawnTank();
			}	
		}
		
		super.update();
		
		ArrayList<Tank> removeSpawned = new ArrayList<Tank>();
		
		for (int i = 0; i < this.spawned.size(); i++)
		{
			if (!Game.movables.contains(this.spawned.get(i)))
				removeSpawned.add(this.spawned.get(i));
		}
		
		for (int i = 0; i < removeSpawned.size(); i++)
		{
			this.spawned.remove(removeSpawned.get(i));
		}
		
		if (Math.random() < 0.003 && this.spawned.size() < 6)
		{
			this.spawnTank();
		}
	}
	
	public void spawnTank()
	{
		double pos = Math.random() * 250;
		int side = (int) (Math.random() * 4);
		
		double x = pos;
		double y = pos;
		
		if (side == 0)
			x = -100;
		else if (side == 1)
			x = 100;
		else if (side == 2)
			y = -100;
		else if (side == 3)
			y = 100;
		
		Tank t = new TankBoss("boss", 0, 0, 0);
		
		while (t instanceof TankBoss)
		{
			t = Game.registryTank.getRandomTank().getTank(this.posX + x, this.posY + y, this.angle);
			t.team = this.team;
		}
		
		Game.events.add(new EventCreateTank(t));
		this.spawned.add(t);
		
		Game.movables.add(t);
	}
}
