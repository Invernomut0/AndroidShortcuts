package com.invernomuto.DualBoot;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import su.levenetc.android.textsurface.Text;
import su.levenetc.android.textsurface.TextBuilder;
import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.animations.Alpha;
import su.levenetc.android.textsurface.animations.Circle;
import su.levenetc.android.textsurface.animations.Delay;
import su.levenetc.android.textsurface.animations.Parallel;
import su.levenetc.android.textsurface.animations.Rotate3D;
import su.levenetc.android.textsurface.animations.Sequential;
import su.levenetc.android.textsurface.animations.ShapeReveal;
import su.levenetc.android.textsurface.animations.SideCut;
import su.levenetc.android.textsurface.animations.Slide;
import su.levenetc.android.textsurface.animations.TransSurface;
import su.levenetc.android.textsurface.contants.Align;
import su.levenetc.android.textsurface.contants.Direction;
import su.levenetc.android.textsurface.contants.Pivot;
import su.levenetc.android.textsurface.contants.Side;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mExplosionField = ExplosionField.attach2Window(this);
        setContentView(R.layout.info_layout);   //final Typeface robotoBlack = Typeface.createFromAsset;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        TextSurface text_surface = findViewById(R.id.text_surface);
        Spanned sLink = Html.fromHtml(getString(R.string.linkxda));
        TextView tlink = findViewById(R.id.tLink);
        //String link = "<a href='http://www.google.com'> Google </a>"
        //tLink.setMovementMethod(LinkMovementMethod.getInstance());
        /*
        NoUnderlineSpan mNoUnderlineSpan = new NoUnderlineSpan();
        if (tlink.getText() instanceof Spannable) {
            Spannable s = (Spannable) tlink.getText();
            s.setSpan(mNoUnderlineSpan, 0, s.length(), Spanned.SPAN_MARK_MARK);
        }*/
        tlink.setText(sLink);
        tlink.setMovementMethod(LinkMovementMethod.getInstance());

        Text DualBoot = TextBuilder
                .create("Dual Boot")
                .setPaint(paint)
                .setSize(64)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.SURFACE_CENTER).build();

        Text Ofox = TextBuilder
                .create("powered by Orangefox")
                .setPaint(paint)
                .setSize(36)
                .setAlpha(0)
                .setColor(Color.rgb(255,150,00))
                .setPosition(Align.BOTTOM_OF, DualBoot).build();

        Text DoubleYour = TextBuilder
                .create("Double your")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF, Ofox).build();

        Text Oneplus = TextBuilder
                .create(" Oneplus power!")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.RIGHT_OF, DoubleYour).build();

        Text BasedOn = TextBuilder
                .create("Based on")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Oneplus).build();

        Text Zack = TextBuilder
                .create("Zackptg5 project")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF, BasedOn).build();

        Text Reborn = TextBuilder
                .create("Reborn by")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Zack).build();

        Text Inverno = TextBuilder
                .create("Invernomut0")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Reborn).build();

        Text Enjoy = TextBuilder
                .create("Enjoy!")
                .setPaint(paint)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Inverno).build();

        Text Telegram = TextBuilder
                .create("Telegram: @inv3rn0mut0")
                .setPaint(paint)
                .setSize(32)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Enjoy).build();
        Text xda = TextBuilder
                .create("XDA: Invernomut0")
                .setPaint(paint)
                .setSize(32)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Telegram).build();

        text_surface.play(
                new Sequential(
                        ShapeReveal.create(DualBoot, 1250, SideCut.show(Side.LEFT), false),
                        new Parallel(ShapeReveal.create(DualBoot, 750, SideCut.hide(Side.LEFT), false), new Sequential(Delay.duration(600), ShapeReveal.create(DualBoot, 600, SideCut.show(Side.LEFT), false))),
                        new Parallel(new TransSurface(500, Ofox, Pivot.CENTER), ShapeReveal.create(Ofox, 1300, SideCut.show(Side.LEFT), false)),
                        Delay.duration(500),
                        new Parallel(new TransSurface(750, DoubleYour, Pivot.CENTER), Slide.showFrom(Side.LEFT, DoubleYour, 750)),
                        Delay.duration(500),
                        new Parallel(TransSurface.toCenter(Oneplus, 500), Rotate3D.showFromSide(Oneplus, 750, Pivot.TOP)),
                        new Parallel(TransSurface.toCenter(BasedOn, 500), Slide.showFrom(Side.TOP, BasedOn, 500)),
                        new Parallel(TransSurface.toCenter(Zack, 750), Slide.showFrom(Side.LEFT, Zack, 500)),
                        Delay.duration(500),
                        new Parallel(
                                new TransSurface(1500, Reborn, Pivot.CENTER),
                                new Sequential(
                                        new Sequential(ShapeReveal.create(Reborn, 500, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Sequential(ShapeReveal.create(Inverno, 500, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Sequential(ShapeReveal.create(Enjoy, 750, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new TransSurface(750, Telegram, Pivot.CENTER), Slide.showFrom(Side.LEFT, Telegram, 1750),
                                        new TransSurface(750, xda, Pivot.CENTER), Slide.showFrom(Side.RIGHT, xda, 1750)


                                )
                        ),
                        Delay.duration(200),
                        new Parallel(
                                ShapeReveal.create(Enjoy, 1500, SideCut.hide(Side.LEFT), true),
                                new Sequential(Delay.duration(500), ShapeReveal.create(Reborn, 1500, SideCut.hide(Side.LEFT), true)),
                                Alpha.hide(Zack, 1500),
                                Alpha.hide(BasedOn, 1500),
                                Alpha.hide(DualBoot, 1500),
                                Alpha.hide(Oneplus, 1500),
                                Alpha.hide(Ofox, 1500)
                                //new Sequential(Delay.duration(250), ShapeReveal.create(textDevilishGang, 1500, SideCut.show(Side.TOP), false))
                                )
                )
        );

    }
    public void onClickInfo(View view) {
        Info.this.finish();
    }

}
