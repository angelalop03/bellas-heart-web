package com.us.TFG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


public class UIStyle {
    private static BitmapFont font;
    private static BitmapFont titleFont;
    private static Skin skin;

    static {
        // Cargar la fuente
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Caveat.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 34;
        font = generator.generateFont(parameter);
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 40;
        titleFont = generator.generateFont(titleParameter);
        generator.dispose();

        // Crear y configurar el Skin
        skin = new Skin();
        skin.add("Caveat", font);
    }

    public static Label.LabelStyle getLabelStyle() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        return labelStyle;
    }

    public static Label.LabelStyle getTitleLabelStyle() {
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = titleFont;
        titleFont.getData().setScale(1.2f);
        titleLabelStyle.fontColor = Color.WHITE;
        return titleLabelStyle;
    }

    public static TextButton.TextButtonStyle getButtonStyle() {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = new TextureRegionDrawable(new Texture(createRoundedPixmap(350, 80, Color.LIGHT_GRAY, 0.4f)));
        buttonStyle.down = new TextureRegionDrawable(new Texture(createRoundedPixmap(350, 80, Color.GRAY, 0.4f)));
        buttonStyle.over = new TextureRegionDrawable(new Texture(createRoundedPixmap(350, 80, Color.DARK_GRAY, 0.4f)));
        return buttonStyle;
    }
    public static void showError(Label feedbackLabel, String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setColor(Color.RED);
    }



    public static TextureRegionDrawable getBackgroundDrawable() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.6f, 0.6f, 0.6f, 0.6f));
        pixmap.fill();
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
        return backgroundDrawable;
    }

    public static TextField.TextFieldStyle getTextFieldStyle() {
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = new TextureRegionDrawable(new Texture(createRoundedPixmap(400, 70, Color.DARK_GRAY, 0.6f)));
        return textFieldStyle;
    }

    public static Pixmap createRoundedPixmap(int width, int height, Color color, float transparency) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(new Color(color.r, color.g, color.b, transparency));
        pixmap.fillRectangle(0, 20, width, height - 40);
        return pixmap;
    }

    public static BitmapFont getFont() {
        return font;
    }

    public static Skin getSkin() {
        return skin;
    }

    public static TextButton.TextButtonStyle getCustomButtonStyle(int width, int height, Color color) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.up = new TextureRegionDrawable(new Texture(createRoundedPixmap(width, height, color, 1.0f)));
        style.down = new TextureRegionDrawable(new Texture(createRoundedPixmap(width, height, Color.DARK_GRAY, 1.0f)));
        style.over = new TextureRegionDrawable(new Texture(createRoundedPixmap(width, height, Color.LIGHT_GRAY, 1.0f)));
        return style;
    }


    public static SelectBox.SelectBoxStyle getSelectBoxStyle() {
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        // Fondo del selectBox cerrado
        style.background = new TextureRegionDrawable(new Texture(createRoundedPixmap(400, 70, Color.DARK_GRAY, 0.6f)));

        // Fondo del desplegable
        style.backgroundOpen = new TextureRegionDrawable(new Texture(createRoundedPixmap(400, 200, Color.DARK_GRAY, 0.8f)));

        // Fondo del desplegable cuando está cerrado
        style.backgroundOver = new TextureRegionDrawable(new Texture(createRoundedPixmap(400, 70, Color.GRAY, 0.6f)));

        // Estilo de la lista
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorSelected = Color.BLACK;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = new TextureRegionDrawable(new Texture(createRoundedPixmap(400, 70, Color.LIGHT_GRAY, 1f)));

        style.listStyle = listStyle;

        // Estilo del scroll
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = new TextureRegionDrawable(new Texture(createRoundedPixmap(6, 200, Color.DARK_GRAY, 0.6f)));
        scrollStyle.vScrollKnob = new TextureRegionDrawable(new Texture(createRoundedPixmap(6, 60, Color.LIGHT_GRAY, 1f)));
        style.scrollStyle = scrollStyle;

        return style;
    }

    //-------------------------------------------------------------------------------------------


    public static Drawable getBoxBackground() {
        Pixmap pixmap = UIStyle.createRoundedPixmap(200, 100, new Color(0.2f, 0.2f, 0.5f, 0.8f), 0.9f);
        return new TextureRegionDrawable(new Texture(pixmap));
    }

    public static Window.WindowStyle getDialogStyle() {
        Window.WindowStyle style = new Window.WindowStyle();
        style.titleFont = getFont();
        style.titleFontColor = Color.WHITE;
        style.background = new TextureRegionDrawable(
            new Texture(UIStyle.createRoundedPixmap(500, 300, new Color(0.3f, 0.3f, 0.5f, 1f), 1f))
        );
        return style;
    }


    public static Texture createColoredTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }


    public static Slider.SliderStyle getSliderStyle() {
        Pixmap pixmap = new Pixmap(300, 20, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1f, 1f, 1f, 0.3f)); // fondo del slider
        pixmap.fill();

        TextureRegionDrawable background = new TextureRegionDrawable(new Texture(pixmap));

        pixmap.dispose();
        pixmap = new Pixmap(10, 20, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.PINK); // color del "knob" o barra de progreso
        pixmap.fill();

        TextureRegionDrawable knob = new TextureRegionDrawable(new Texture(pixmap));
        TextureRegionDrawable knobBefore = new TextureRegionDrawable(new Texture(pixmap)); // parte llenada

        pixmap.dispose();

        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = background;
        style.knob = knob;
        style.knobBefore = knobBefore;

        return style;
    }



}
