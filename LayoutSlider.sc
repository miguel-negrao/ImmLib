LayoutSlider{

    *new{ |label|
        var sl =  Slider().orientation_(\horizontal).maxHeight_(30);

        ^[HLayout(
            sl,
            StaticText().string_(label).maxHeight_(30)
        ),sl];
    }
}