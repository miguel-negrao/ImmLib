/*
ImmLib
Copyright 2013 Miguel Negrao.

ImmLib: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GameOfLife Unit Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

ImmUChain : ParUChain {
	var <surface;
	var <surfaceKey;

	*new { |surfaceKey...args|
		var surface = PSurfaceDef.get(surfaceKey);
		var createImmUChain = { arg surface, surfaceKey, args;
			super.prBasicNew()
			.initImmUChain( surface, surfaceKey, args )
		};
		var withPanners = {
			var busses = ParArg( surface.ubuses.items );
			createImmUChain.(surface, surfaceKey, args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] )
		};
		^switch(ImmLib.mode)
		{\normal}{
			^switch(surface.renderMethod)
			{\vbap} {
				withPanners.()
			}
			{\direct}{
				var busses = ParArg( surface.renderOptions.spkIndxs );
				createImmUChain.(surface, surfaceKey, args ++ [ [\output, [\bus, busses] ] ] )
			}
			{ Error("PSurface renderMethod unknown : %.\nHas to be either \vbap, \vbapTest or \direct".format(surface.renderMethod)).throw }
		}
		{\previewStereo}{
			var points = ParArg( surface.pointsRV3D.collect{ |p| Point(p.x, p.y) } );
			createImmUChain.(surface, surfaceKey, args ++ [ [\stereoOutput, [\point, points] ] ] )
		}
		{ Error("ImmLib.mode unknown : %.\nHas to be either \normal or \previewStereo !".format(ImmLib.mode)).throw }

	}

	initImmUChain { |asurface, asurfaceKey, args|

		var tempDur;
		//"initImmUChain surface %".format(asurface).postln;
		surface = asurface;
		surfaceKey = asurfaceKey;
		n = surface.size;

		if( args[0].isNumber ) {
			startTime = args[0];
			args = args[1..]
		};
		if( args[0].isNumber ) {
			track = args[0];
			args = args[1..]
		};
		if( args[0].isNumber ) {
			tempDur = args[0];
			args = args[1..]
		};
		if( args[0].class.superclass == Boolean ) {
			releaseSelf = args[0]; args = args[1..]
		};

		units = args.collect(_.asImmUnit(n, surface )); //only thing different
		if( tempDur.notNil ) { this.duration = tempDur };

		prepareTasks = [];

		units.reverse.do(_.uchainInit( this ));

		this.changed( \init );
	}

	*newNoPanner { |surfaceKey...args|
		var surface = PSurfaceDef.get(surfaceKey);
		^super.prBasicNew()
		.initImmUChain( surface, surfaceKey, args )
	}

	getInitArgs {
		var numPreArgs = -1;
		if( releaseSelf != true ) {
			numPreArgs = 3
		} {
			if( duration != inf ) {
				numPreArgs = 2
			} {
				if( track != 0 ) {
					numPreArgs = 1
				} {
					if( startTime != 0 ) {
						numPreArgs = 0
					}
				}
			}
		};

		^([ surfaceKey, startTime, track, duration, releaseSelf ][..(numPreArgs+1)]) ++
		units
		.select{ |unit| ['pannerout','output','stereoOutput'].includes(unit.def.name).not }
		.collect({ |item|
			item = item.storeArgs;
			if( item.size == 1 ) {
				item[0]
			} {
				item[1..]
			};
		});
	}

	storeArgs { ^this.getInitArgs }

	storeModifiersOn { |stream|
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
		if( ugroup.notNil ) {
			stream << ".ugroup_(" <<< ugroup << ")";
		};
		if( serverName.notNil ) {
			stream << ".serverName_(" <<< serverName << ")";
		};
		if( addAction != \addToHead ) {
			stream << ".addAction_(" <<< addAction << ")";
		};
		if( global != false ) {
			stream << ".global_(" <<< global << ")";
		};
		if( this.fadeIn != 0.0 ) {
			stream << ".fadeIn_(" <<< this.fadeIn << ")";
		};
		if( this.fadeOut != 0.0 ) {
			stream << ".fadeOut_(" <<< this.fadeOut << ")";
		};
		if( this.fadeInCurve != 0.0 ) {
			stream << ".fadeInCurve_(" <<< this.fadeInCurve << ")";
		};
		if( this.fadeOutCurve != 0.0 ) {
			stream << ".fadeOutCurve_(" <<< this.fadeOutCurve << ")";
		};
		if( this.gain != 0.0 ) {
			stream << ".gain_(" <<< this.gain << ")";
		};
	}


	createNewUnitFromSymbol { |symbol|
		^ImmU( surface, View.currentDrag )
	}


}
