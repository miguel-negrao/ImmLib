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

ImmUChain : MUChain {
	var <surface;

	*new { |surface...args|
		var f = {
			//var ssa = "surface.size = %".format(surface.size).postln;
			//var busses = ClusterArg( surface.size.collect(500 + _) );
			var busses = surface.ubuses;
			super.new( *( args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] ) ).releaseSelf_(false).initImmMUChain(surface)
		};
		if(surface.isKindOf(PSurface).not) {
			Error("First argument of MUChain must be a PSurface").throw
		};
		^switch(ImmLib.mode)
		{\normal}{
			^switch(surface.renderMethod)
			{\vbap} {
				f.()
			}
			{\vbapTest} {
				f.()
			}
			{\direct}{
				var busses = ClusterArg( surface.renderOptions.spkIndxs );
				super.new( *( args ++ [ [\output, [\bus, busses] ] ] ) )
				.releaseSelf_(false)
				.initImmMUChain(surface)
			}
			{ Error("PSurface renderMethod unknown : %.\nHas to be either \vbap, \vbapTest or \direct".format(surface.renderMethod)).throw }
		}
		{\previewStereo}{
			var points = ClusterArg( surface.pointsRV3D.collect{ |p| Point(p.x, p.y) } );
			super.new( *( args ++ [ [\stereoOutput, [\point, points ] ] ] ) ).initImmMUChain(surface)
		}
		{ Error("ImmLib.mode unknown : %.\nHas to be either \normal or \previewStereo !".format(ImmLib.mode)).throw }


	}

	initImmMUChain { |asurface|
		surface = asurface
	}

	getInitArgs {
		var numPreArgs = -1;
		var unitStoreArgs;
		"getInitArgs".postln;

		if( this.releaseSelf != true ) {
			numPreArgs = 3
		} {
			if( this.duration != inf ) {
				numPreArgs = 2
			} {
				if( this.track != 0 ) {
					numPreArgs = 1
				} {
					if( this.startTime != 0 ) {
						numPreArgs = 0
					}
				}
			}
		};

		unitStoreArgs =  { |unitArray, mod, i|
			var unit = unitArray[0];

			var def = if( unit.def.class.callByName ) {
				unit.defName
			} {
				unit.def
			};

			var defArgs = (unit.def.args( unit ) ? []).clump(2);

			var args = unitArray.collect{ |x| x.args.clump(2) }.flop.collect{ |uargArray|
				var values = uargArray.flop.at(1);
				if( (values.as(Set).as(Array).size == 1) ) {
					uargArray[0]
				} {
					[uargArray[0][0], values.carg]
				}
			}.select({ |item, i|
				(item[1].class != ClusterArg) && (item != defArgs[i]) && { unit.dontStoreArgNames.includes( item[0] ).not };
			}).collect({ |item|
				var umapArgs;
				if( item[1].isUMap ) {
					umapArgs = item[1].storeArgs;
					if( umapArgs.size == 1 ) {
						[ item[0], umapArgs[0] ]
					} {
						[ item[0], umapArgs ]
					};
				} {
					item
				};
			}).flatten(1);

			if(mod.isDefined){
				[def, args, mod.get]
			} {
				[def, args]
			};
		};

		^([ surface, this.startTime, this.track, this.duration, this.releaseSelf ][..(numPreArgs+1)]) ++
		[items.collect(_.units).flop, mods, (1..mods.size)-1]
		.flopWith( unitStoreArgs )
		//very hacky !
		.select{ |xs| ['pannerout','output','stereoOutput'].includes(xs[0].postln).not }
		.collect{ |xs|
			if(xs[1].size == 0) {
				xs[0]
			}{
				xs
			}
		}
	}

	storeModifiersOn { |stream|
		items[0].storeTags( stream );
		items[0].storeDisplayColor( stream );
		items[0].storeDisabledStateOn( stream );
		if( items[0].global != false ) {
			stream << ".global_(" <<< items[0].global << ")";
		};
		if( items[0].fadeIn != 0.0 ) {
			stream << ".fadeIn_(" <<< items[0].fadeIn << ")";
		};
		if( items[0].fadeOut != 0.0 ) {
			stream << ".fadeOut_(" <<< items[0].fadeOut << ")";
		};
	}

}
		