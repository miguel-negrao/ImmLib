ParArrayControlSpec : ArrayControlSpec {

	makeView { |parent, bounds, label, action, resize, hasMod = false|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 40, operationsOffset = 1;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \val ] = ParArg(default.asCollection);
		vws[ \range ] = [ vws[ \val ].array.minItem, vws[ \val ].array.maxItem ];
		vws[ \doAction ] = { action.value( vws, vws[ \val ].array ) };

		vws[ \operations ] = OEM(
			\edit, { |values|
				var plotter;
				if( vws[ \plotter ].isNil or: { vws[ \plotter ].parent.isClosed } ) {
					plotter = vws[ \val ].array.plot;
					plotter.editMode_( true )
					.specs_( this )
					.findSpecs_( false )
					.plotMode_( \points )
					.editFunc_({ |vw|
						vws[ \val ] = ParArg(vw.value);
						vws[ \range ] = [ vws[ \val ].array.minItem, vws[ \val ].array.maxItem ];
						vws[ \setRangeSlider ].value;
						vws[ \setMeanSlider ].value;
						action.value( vws, vws[ \val ] );
					});

					plotter.parent.onClose = plotter.parent.onClose.addFunc({
						if( vws[ \plotter ] == plotter ) {
							vws[ \plotter ] = nil;
						};
					});
					vws[ \plotter ] = plotter;
				} {
					vws[ \plotter ].parent.front;
				};
				values;
			},
			\invert, { |values|
				values = this.unmap( values );
				values = values.linlin(
					values.minItem, values.maxItem, values.maxItem, values.minItem
				);
				this.map( values );
			},
			\reverse, { |values|
				values.reverse;
			},
			\sort, { |values|
				values.sort;
			},
			\scramble, { |values|
				values.scramble;
			},
			\rotate, { |values|
				values.rotate(1);
			},
			\squared, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).squared
					.linlin(0, 1, min, max )
				);
			},
			\sqrt, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).sqrt
					.linlin(0, 1, min, max )
				);
			},
			\scurve, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).scurve
					.linlin(0, 1, min, max )
				);
			},
			\flat, {|values|
				var mean;
				mean = values.mean;
				mean ! (values.size);
			},
			\random, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				if( min == max ) { max = vws[ \rangeSlider ].rangeSlider.hi };
				values = values.collect({ 0.0 rrand: 1 }).normalize(min, max);
				this.map( values );
			},
			\line, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				if( min == max ) { max = vws[ \rangeSlider ].hi; };
				values = (0..values.size-1).linlin(0,values.size-1, min, max );
				this.map( values );
			}
		);

		[ 0.1, 1, 10, 100, 1000 ].do({ |item|
			if( (step < item) && { (maxval - minval) >= item } ) {
				vws[ \operations ][ "round(%)".format(item).asSymbol ] = { |values|
					this.constrain( values.round(item) );
				};
			};
		});

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
			.string_( label.asString ++ " " )
			.align_( \right )
			.resize_( 4 )
			.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};

		vws[ \rangeSlider ] = EZSmoothRanger( view, (width - 84) @ (bounds.height),
			nil, this, { |sl|
				var values, min, max;
				values = this.unmap( vws[ \val ].array );
				vws[ \range ] = sl.value;
				#min, max = this.unmap( vws[ \range ] );
				if( min == max ) { max = max + 1.0e-11 };
				values = values.linlin( values.minItem, values.maxItem, min, max );
				if( values.every(_==min) ) {
					values = Array.series( values.size, min, ((max - min)/(values.size-1)) );
				};
				vws[ \val ] = ParArg( this.map( values ) );
				vws[ \setPlotter ].value;
				vws[ \setMeanSlider ].value;
				action.value( vws, vws[ \val ] );
			}
		);

		vws[ \setRangeSlider ] = {
			var min, max;
			min = vws[ \val ].array.minItem;
			max = vws[ \val ].array.maxItem;
			vws[ \rangeSlider ].value_( [ min, max ] );
		};

		vws[ \setRangeSlider ].value;

		vws[ \meanSlider ] = SmoothSlider(
			vws[ \rangeSlider ].rangeSlider.parent,
			vws[ \rangeSlider ].rangeSlider.bounds.insetAll(0,0,0,
				vws[ \rangeSlider ].rangeSlider.bounds.height * 0.6 )
		)
		.hiliteColor_( nil )
		.background_( Color.white.alpha_(0.125) )
		.knobSize_(0.6)
		.mode_( \move )
		.action_({ |sl|
			var values, min, max, mean;
			values = this.unmap( vws[ \val ].array );
			min = values.minItem;
			max = values.maxItem;
			mean = [ min, max ].mean;
			values = values.normalize( *(([ min, max ] - mean) + sl.value).clip(0,1) );
			vws[ \val ] = ParArg( this.map( values ) );
			vws[ \setPlotter ].value;
			vws[ \setRangeSlider ].value;
			action.value( vws, vws[ \val ] );
		});

		vws[ \meanSlider ].mouseDownAction = { |sl, x,y,mod, xx, clickCount|
			if( clickCount == 2 ) {
				vws[ \val ] = ParArg( this.map( sl.value ) ! vws[ \val ].array.size );
				vws[ \setRangeSlider ].value;
				vws[ \setPlotter ].value;
			};
		};

		vws[ \setMeanSlider ] = {
			var min, max;
			min = vws[ \val ].array.minItem;
			max = vws[ \val ].array.maxItem;
			vws[ \meanSlider ].value_( this.unmap( [ min, max ] ).mean );
		};

		vws[ \setMeanSlider ].value;

		if( GUI.id === \qt ) { optionsWidth = 80; operationsOffset = 0; };

		vws[ \options ] = PopUpMenu( view, (optionsWidth-if(hasMod){18}{0}) @ (bounds.height) )
		.items_( [ "do", " " ] ++ vws[ \operations ].keys[operationsOffset..] )
		.font_( font )
		.applySkin( RoundView.skin )
		.action_({ |vw|
			var func;
			func = vws[ \operations ][ vw.item ];
			if( func.notNil ) {
				vws[ \val ] = ParArg( func.(vws[ \val ].array) );
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
			};
			vw.value = 0;
		});

		if( GUI.id != \qt ) {
			vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "edit" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				vws[ \operations ][ \edit ].value;
			});
			vws[ \edit ].resize_(3);
		};

		vws[ \setPlotter ] = {
			if( vws[ \plotter ].notNil ) {
				{ vws[ \plotter ].value = vws[ \val ].array; }.defer;
			};
		};

		if( hasMod ) {
			vws[ \updateCheckbox ] = SmoothButton( view, 14 @ (bounds.height) )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.label_( [ "", 'x' ] );
		};

		vws[ \update ] = {
			if( hasMod.not or: { vws[ \updateCheckbox ].value == 1 } ) {
				vws[ \setRangeSlider ].value;
				vws[ \setMeanSlider ].value;
				vws[ \setPlotter ].value;
			};
		};

		vws[ \rangeSlider ].view.resize_(2);
		vws[ \meanSlider ].resize_(2);
		vws[ \options ].resize_(3);

		view.view.onClose_({
			if( vws[ \plotter ].notNil ) {
				vws[ \plotter ].parent.close
			};
		});

		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \val ] = value;
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}

	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value.array), active );
	}

}

+ Spec {

	parEditSpec { ^nil }

}

+ ControlSpec {
	parEditSpec { |inArray, n|
		^ParArrayControlSpec.newFrom(this).default_( inArray.sum / n );
	}
}

+ RangeSpec {
	parEditSpec { ^nil }
}

+ ArrayControlSpec {
	parEditSpec { ^nil }
}

+ AngleSpec {
	parEditSpec { ^nil }
}


