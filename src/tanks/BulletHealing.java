package tanks;

import tanks.AttributeModifier.Operation;
import tanks.event.EventShootBullet;
import tanks.tank.Tank;

public class BulletHealing extends BulletInstant
{
	public BulletHealing(double x, double y, int bounces, Tank t) 
	{
		super(x, y, bounces, t, false, false);
		t.liveBullets--;
		this.playPopSound = false;
		this.baseColorR = 0;
		this.baseColorG = 255;
		this.baseColorB = 0;
		this.name = "heal";
		this.effect = BulletEffect.none;
		this.damage = -0.01 * Panel.frameFrequency;
	}
	
	/** Do not use, instead use the constructor with primitive data types. */
	@Deprecated
	public BulletHealing(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x, y, bounces, t);
		this.item = ib;
		this.item.liveBullets--;
	}
	
	@Override
	public void update()
	{
		this.shoot();
		Game.removeMovables.add(this);
	}
	
	public void shoot()
	{
		if (!tank.isRemote)
		{
			BulletHealing b = new BulletHealing(this.posX, this.posY, this.bounces, this.tank);
			b.vX = this.vX;
			b.vY = this.vY;
			Game.events.add(new EventShootBullet(b));
		}
		
		while(!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;
			
			super.update();
			Game.effects.add(Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.healing));
		}

		if (Game.fancyGraphics)
		{
			for (int i = 0; i < this.size * 4; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
				int var = 50;
				e.maxAge /= 2;
				e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
				Game.effects.add(e);
			}
		}
	}
	
	@Override
	public void collidedWithTank(Tank t)
	{
		if (!heavy)
			this.destroy = true;
		
		t.lives = Math.min(t.baseLives + 1, t.lives - this.damage);
		t.attributes.add(new AttributeModifier("healray", "healray", Operation.add, 1.0));
	}
	
	@Override
	public void draw()
	{
		
	}
	
	@Override
	public void collidedWithObject(Movable o)
	{
		
	}
}
