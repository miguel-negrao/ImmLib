/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

SingleObjectView : ObjectView {

	var <>redrawUChainGUIAction;

	makeView { | bounds, controller, label |
		var x, createdController = false, setter;
		//var newBounds = bounds.asRect.insetAll(18,0,0,0);
		var newBounds = bounds.asRect;
		controller = controller ?? {
			createdController = true;
			SimpleController( object );
		};

		composite = CompositeView( parent, newBounds ).resize_(2);
		composite.onClose = {
			controller.put( key, nil );
			if( createdController ) { controller.remove };
		};

		SmoothButton(composite, Rect(newBounds.width-14,0,14,newBounds.height) )
		.states_([["n"]])
		.action_{
			//object.perform( key.asSetter, ParArg( object.perform( key.asGetter ).dup(object.n) ) );
			var index = object.keys.indexOf( key );
			var value = ParArg( object.perform( key.asGetter ).dup(object.n) );
			object.args[ (index * 2) + 1 ] = value;
			redrawUChainGUIAction.();
		};

		setter = key.asSetter;

		//views = (valueView:x);
		views = spec.makeView( composite, Rect(0,0, newBounds.width-14-4, newBounds.height), label ? key,
			{ |vw, value|
				object.perform( key.asSetter, value );
				action.value( this, value );
		}, 5 );




		this.update;

		controller.put( key, { |obj, key, value|
			if( testValue.isNil or: { testValue.(value) } ) {
				spec.setView( views, value, false );
			};
		});

	}
}

ParObjectView : ObjectView {

	var <>redrawUChainGUIAction;
	var <>pausable;

	makeView { | bounds, controller, label |
		var x, createdController = false, setter;
		//var newBounds = bounds.asRect.insetAll(18,0,0,0);
		var newBounds = bounds.asRect;
		controller = controller ?? {
			createdController = true;
			SimpleController( object );
		};

		composite = CompositeView( parent, newBounds ).resize_(2);
		composite.onClose = {
			controller.put( key, nil );
			if( createdController ) { controller.remove };
		};

		SmoothButton(composite, Rect(newBounds.width-14,0,14,newBounds.height) )
		.states_([["1"]])
		.action_{
			var index;
			var vals = object.perform( key.asGetter ).array;

			var val = if( vals[0].isNumber ) {
				vals.sum / vals.size
			} {
				vals[0]
			};
			//object.perform( key.asSetter, val );
			index = object.keys.indexOf( key );
			object.args[ (index * 2) + 1 ] = val;
			object.def.setSynth( object, *[key, val] );
			redrawUChainGUIAction.();
		};

		setter = key.asSetter;

		views = spec.makeView( composite, Rect(0,0, newBounds.width-14-4, newBounds.height), label ? key,
			{ |vw, value|
				object.perform( key.asSetter, value );
				action.value( this, value );
		}, 5, (object.mod !? { |mod| mod.keySignalDict }) !? { |dict| dict.keys.includes(key) } ?? false );

		this.update;

		controller.put( key, { |obj, key, value|
			if( testValue.isNil or: { testValue.(value) } ) {
				spec.setView( views, value, false );
			};
		});

	}
}

ParStaticObjectView : ObjectView {

	var <>redrawUChainGUIAction;

	makeView { | bounds, controller, label |
		var x, createdController = false, setter, st;
		//var newBounds = bounds.asRect.insetAll(18,0,0,0);
		var newBounds = bounds.asRect;
		controller = controller ?? {
			createdController = true;
			SimpleController( object );
		};

		composite = CompositeView( parent, newBounds ).resize_(2);
		composite.onClose = {
			controller.put( key, nil );
			if( createdController ) { controller.remove };
		};

		/*
		put here button "edit"

		{
						var allUnits, userClosed = true;
						if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
							massEditWindow.close;
						};
						RoundView.pushSkin( skin );
						massEditWindow = Window( unit.defName,
							this.window.bounds.moveBy( this.window.bounds.width + 10, 0 ),
							scroll: true ).front;
						massEditWindow.addFlowLayout;
						comp.onClose_({
							if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
								userClosed = false;
								massEditWindow.close;
							};
						});
						allUnits = unit.units.collect({ |item, ii|
							var ugui;
							if( notMassEdit ) { ii = ii + (realIndex - unit.units.size) };
							StaticText( massEditWindow,
									(massEditWindow.bounds.width - 8 - scrollerMargin) @ 14 )
								.applySkin( RoundView.skin )
								.string_( " " ++ ii ++ ": " ++ item.defName )
								.background_( Color.white.alpha_(0.5) )
								.resize_(2)
								.font_(
									(RoundView.skin.tryPerform( \at, \font ) ??
										{ Font( Font.defaultSansFace, 12) }).boldVariant
								);
							massEditWindow.view.decorator.nextLine;
							ugui = item.gui( massEditWindow );
							ugui.mapSetAction = {
								chain.changed( \units );
							};
							[ item ] ++ item.getAllUMaps;
						}).flatten(1);
						allUnits.do({ |item|
							item.addDependant( unitInitFunc )
						});
						massEditWindowIndex = i;
						massEditWindow.onClose_({
							allUnits.do(_.removeDependant(unitInitFunc));
							if( userClosed ) {
								massEditWindowIndex = nil;
							};
						});
						RoundView.popSkin( skin );
					}
		*/

		SmoothButton(composite, Rect(newBounds.width-14,0,14,newBounds.height) )
		.states_([["1"]])
		.action_{
			var index;
			var vals = object.perform( key.asGetter ).array;

			var val = if( vals[0].isNumber ) {
				vals.sum / vals.size
			} {
				vals[0]
			};
			//object.perform( key.asSetter, val );
			index = object.keys.indexOf( key );
			object.args[ (index * 2) + 1 ] = val;
			object.def.setSynth( object, *[key, val] );
			redrawUChainGUIAction.();
		};

		setter = key.asSetter;

		st = StaticText( composite, Rect(0,0, 80, 14 ) )
		.string_( key.asString ++ " ")
		.align_( \right )
		.resize_( 4 )
		.applySkin( RoundView.skin );

		views = (view: (value_:{}),value:{}, \valueView:(value_:{}, value:{}));

		this.update;

		controller.put( key, { |obj, key, value|
			if( testValue.isNil or: { testValue.(value) } ) {
				spec.setView( views, value, false );
			};
		});

	}
}
