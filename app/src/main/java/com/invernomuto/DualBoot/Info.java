package com.invernomuto.DualBoot;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.guillotine_background_dark));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.guillotine_background_dark));
        }
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

        Text tx = TextBuilder
                .create("Thanks to:")
                .setPaint(paint)
                .setPadding(0,64,0,0)
                //.setPosition(200,600)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Zack).build();

        Text flo = TextBuilder
                .create("Florian (@Wishmasterflo)")
                .setPaint(paint)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, tx).build();

        Text flo2 = TextBuilder
                .create("OP6 DualBoot Creator")
                .setPaint(paint)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, flo).build();


        Text tx2 = TextBuilder
                .create("Special thanks to:")
                .setPaint(paint)
                .setPadding(0,64,0,0)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.RED)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, flo2).build();

        Text n1 = TextBuilder
                .create("John (@Tomkumaton_FAJITA)")
                .setPaint(paint)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, tx2).build();

        Text n2 = TextBuilder
                .create("Ivar (@Ivar418)")
                .setPaint(paint)
                .setSize(24)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, n1).build();

        Text Reborn = TextBuilder
                .create("Reborn by")
                .setPaint(paint)
                .setPadding(0,64,0,0)
                .setSize(44)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, n2).build();

        Text Inverno = TextBuilder
                .create("Invernomut0")
                .setPadding(0,80,0,0)
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
                .create("Oneplus 7 series")
                .setPaint(paint)
                .setPadding(0,64,0,0)
                .setSize(28)
                .setAlpha(0)
                .setColor(Color.WHITE)
                .setPosition(Align.BOTTOM_OF | Align.CENTER_OF, Telegram).build();


        text_surface.play(
                new Sequential(
                        ShapeReveal.create(DualBoot, 1250, SideCut.show(Side.LEFT), false),
                        new Parallel(ShapeReveal.create(DualBoot, 750, SideCut.hide(Side.LEFT), false), new Sequential(Delay.duration(400), ShapeReveal.create(DualBoot, 600, SideCut.show(Side.LEFT), false))),
                        new Parallel(new TransSurface(700, Ofox, Pivot.CENTER), ShapeReveal.create(Ofox, 700, SideCut.show(Side.LEFT), false)),
                        Delay.duration(500),
                        new Parallel(new TransSurface(750, DoubleYour, Pivot.CENTER), Slide.showFrom(Side.LEFT, DoubleYour, 750)),
                        Delay.duration(500),
                        new Parallel(TransSurface.toCenter(Oneplus, 500), Rotate3D.showFromSide(Oneplus, 500, Pivot.TOP)),
                        new Parallel(TransSurface.toCenter(BasedOn, 500), Slide.showFrom(Side.TOP, BasedOn, 500)),
                        new Parallel(TransSurface.toCenter(Zack, 550), Slide.showFrom(Side.LEFT, Zack, 500)),
                        Delay.duration(500),
                        Delay.duration(500),
                        new Sequential(
                                //new TransSurface(100, tx, Pivot.CENTER),
                                new Sequential(
                                    new Sequential(TransSurface.toCenter(tx, 500), Slide.showFrom(Side.LEFT, tx, 500)),
                                    Delay.duration(500),
                                    new Parallel(ShapeReveal.create(flo, 500, Circle.show(Side.CENTER, Direction.CLOCK), false)),
                                    new Parallel(ShapeReveal.create(flo2, 500, Circle.show(Side.CENTER, Direction.COUNTER_CLOCK), false)),
                                    Delay.duration(500),
                                    new Sequential(TransSurface.toCenter(tx2, 500), Slide.showFrom(Side.LEFT, tx2, 500)),
                                    Delay.duration(500),
                                    new Parallel(TransSurface.toCenter(n1, 500), Slide.showFrom(Side.RIGHT, n1, 500)),
                                    new Parallel(TransSurface.toCenter(n2, 500), Slide.showFrom(Side.RIGHT, n2, 500)),
                                    Delay.duration(1000)
                                )
                        ),
                        new Sequential(
                                new TransSurface(500, Reborn, Pivot.CENTER),
                                new Sequential(
                                        new Parallel(ShapeReveal.create(Reborn, 500, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Parallel(ShapeReveal.create(Inverno, 500, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new Parallel(ShapeReveal.create(Enjoy, 750, Circle.show(Side.CENTER, Direction.OUT), false)),
                                        new TransSurface(750, Telegram, Pivot.CENTER), Slide.showFrom(Side.LEFT, Telegram, 1750),
                                        new TransSurface(750, xda, Pivot.CENTER), Slide.showFrom(Side.RIGHT, xda, 1750)


                                )
                        ),
                        Delay.duration(200),
                        new Sequential(
                                ShapeReveal.create(Enjoy, 500, SideCut.hide(Side.LEFT), true),
                                new Sequential(Delay.duration(500), ShapeReveal.create(Reborn, 500, SideCut.hide(Side.LEFT), true)),
                                Alpha.hide(Zack, 1500),
                                Alpha.hide(BasedOn, 1500),
                                Alpha.hide(DualBoot, 1500),
                                Alpha.hide(Oneplus, 1500),
                                Alpha.hide(Ofox, 1500)
                                //new Sequential(Delay.duration(250), ShapeReveal.create(textDevilishGang, 1500, SideCut.show(Side.TOP), false))
                                )
                )
        );
        /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tlink.getLayoutParams();
        params.height = 0;
        tlink.setLayoutParams(params);*/
 /*       @SuppressLint("ResourceType") RelativeLayout layout = (RelativeLayout)findViewById(R.layout.info_layout);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView image = (ImageView) inflater.inflate(R.layout.logo_layout, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, 345);
        params.leftMargin = 32*2*3;
        params.topMargin = 34*2*3;
        layout.addView(image);*/

    }
    public void onClickInfo(View view) {
        Info.this.finish();
    }

}
