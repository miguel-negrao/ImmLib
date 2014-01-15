
ImmU : MassEditU { // mimicks a real U, but in fact edits multiple instances of the same

	init { |inUnits|
		var firstDef, defs;
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
}


ImmUChain : MassEditUChain {

	init {
		var allDefNames = [], allUnits = Order();

		uchains.do({ |uchain|
			uchain.units.select({|x| x.def.class != LocalUdef}).do({ |unit|
				var defName, index;
				defName = unit.defName;
				if( allDefNames.includes( defName ).not ) {
					allDefNames = allDefNames.add( defName );
				};
				index = allDefNames.indexOf( defName );
				allUnits.put( index, allUnits[ index ].add( unit ) );
			});
		});

		units = allUnits.asArray.collect({ |item, i|
			if( allDefNames[i].notNil ) {
				if( item.size == 1 ) {
					item[0];
				} {
					//differs from Mass here
					ImmU( item );
				};
			} {
				nil
			};
		}).select(_.notNil);

		this.changed( \init );
	}
}

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