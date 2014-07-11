
ImmU : MassEditU { // mimicks a real U, but in fact edits multiple instances of the same

	*new { |units, mod| // all units have to be of the same Udef
		^super.newCopyArgs.init(units, mod);
	}

	init { |inUnits, amod|
		var firstDef, defs;
		mod = amod;
		units = inUnits.asCollection;
		defs = inUnits.collect(_.def);
		firstDef = defs[0];
		if( defs.every({ |item| item == firstDef }) ) {
			def = firstDef;
			argSpecs = def.argSpecs.collect({ |argSpec|
				var values, massEditSpec, value;
				values = units.collect({ |unit|
					unit.get( argSpec.name );
				});

				if( values.any(_.isUMap) ) {
					massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
				} {
					//differs from Mass here
					massEditSpec = argSpec.spec.immEditSpec( values );
				};
				if( massEditSpec.notNil ) {
					ArgSpec( argSpec.name, massEditSpec.default, massEditSpec, argSpec.private, argSpec.mode );
				} {
					nil;
				};
			}).select(_.notNil);
			args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
			this.changed( \init );
		} {
			"MassEditU:init - not all units are of the same Udef".warn;
		};
	}

	def {
		^units[0].defName
	}

	defName {
		^units[0].defName
	}
}


ImmMassUChain : MassEditUChain {

	*new { |uchains, mods|
		^super.newCopyArgs( uchains ).initImmMassUChain(mods);
	}

	initImmMassUChain { |mods|
		if(mods.notNil) {
			units = [mods, uchains.collect(_.units).flop].flopWith{ |modOption, units|
				ImmU( units, modOption.orNil )
			}
		} {
			units = uchains.collect(_.units).flop.collect{ | units|
				ImmU( units )
			}
		};

		this.changed( \init );
	}
}

/*ImmUEnvSpec : UEnvSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var skin;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		skin = RoundView.skin;

		bounds.isNil.if{bounds= 160@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;
		vws[ \val ] = Env();

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
			.string_( label.asString ++ " " )
			.align_( \right )
			.resize_( 4 )
			.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
		.label_( "edit" )
		.border_( 1 )
		.radius_( 2 )
		.font_( font )
		.action_({
			var editor;
			if( vws[ \editor ].isNil or: { vws[ \editor ].isClosed } ) {
				RoundView.pushSkin( skin );
				editor = EnvView( "Envelope editor - "++label, env: vws[ \val ].at(0), spec: spec )
				.action_({ |envview| vws[ \val ] })
				.onClose_({
					if( vws[ \editor ] == editor ) {
						vws[ \editor ] = nil;
					};
				});
				RoundView.popSkin;
				vws[ \editor ] = editor;
			} {
				vws[ \editor ].front;
			};

		});

		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].close;
			};
		});

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		if( view[ \editor ].notNil ) {
			view[ \editor ].env = value.at(0);
		};
	}

}*/

ImmArrayControlSpec : ArrayControlSpec {


	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 40, operationsOffset = 1;
		var size = default.size;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \val ] = default.asCollection;
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		vws[ \valueView ] =
		EZSmoothSlider( view, (width - 44) @ (bounds.height), label !? { label.asString ++ " " },
			this, { |vw|
				vws[ \val ] = vw.value.dup(size);
				vws[ \setPlotter ].value;
				action.value( vw, vws[ \val ] )
			},
			labelWidth: (RoundView.skin ? ()).labelWidth ? 80 );

		vws[ \view ] = vws[ \valueView ].view;
		vws[ \sliderView ] = vws[ \valueView ].sliderView;
		vws[ \sliderView ].centered_( true ).centerPos_( this.unmap( default ) );

		vws[ \setValueView ] = {
			var min, max;
			min = vws[ \val ].minItem;
			max = vws[ \val ].maxItem;
			vws[ \valueView ].value_( (min+max)/2 );
		};

		vws[ \setValueView ].value;

		vws[ \edit ] = SmoothButton( view, 20 @ (bounds.height) )
		.label_( "p" )
		.border_( 1 )
		.radius_( 2 )
		.font_( font )
		.action_({
			var values = vws[ \val ];
			var plotter;
			if( vws[ \plotter ].isNil or: { vws[ \plotter ].parent.isClosed } ) {
				plotter = vws[ \val ].plot;
				plotter.editMode_( true )
				.specs_( this )
				.findSpecs_( false )
				.plotMode_( \points )
				.editFunc_({ |vw|
					vws[ \val ] = vw.value;
					vws[ \setValueView ].value;
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
		});
		vws[ \edit ].resize_(3);

		vws[ \setPlotter ] = {
			if( vws[ \plotter ].notNil ) {
				{ vws[ \plotter ].value = vws[ \val ]; }.defer;
			};
		};

		vws[ \updateCheckbox ] = SmoothButton( view, 20 @ (bounds.height) )
		.border_( 1 )
		.radius_( 2 )
		.font_( font )
		.label_( [ "", 'x' ] );

		vws[ \update ] = {
			if( vws[ \updateCheckbox ].value == 1 ) {
				vws[ \setValueView ].value;
				vws[ \setPlotter ].value;
			}
		};

		view.view.onClose_({
			if( vws[ \plotter ].notNil ) {
				vws[ \plotter ].parent.close
			};
		});

		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection;
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}

	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value), active );
	}

}

+ Object {

	immEditSpec { |...args|
		^this.massEditSpec(*args)
	}

	*immEditSpec { |...args|
		^this.massEditSpec(*args)
	}

}

+ ControlSpec {

	asImmArrayControlSpec { ^ImmArrayControlSpec.newFrom( this ) }

	immEditSpec { |inArray|
		^this.asImmArrayControlSpec.default_( inArray );
	}

}


