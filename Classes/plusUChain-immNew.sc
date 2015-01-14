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
	var <surfaceKey;

	*new { |surfaceKey...args|
		var surface = PSurfaceDef.get(surfaceKey);
		var g = { arg surface, surfaceKey, args;
			//"g: surface: %".format(surface).postln;
			this.doesNotUnderstand(*[\new]++this.prStripUMods(args) )
			.initImmUChain( args, this.prGetUmods(args), surface, surfaceKey )
		};
		var f = {
			//var ssa = "surface.size = %".format(surface.size).postln;
			//var busses = ClusterArg( surface.size.collect(500 + _) );
			var busses = surface.ubuses;
			g.(surface, surfaceKey, args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] )//.releaseSelf_(false)
			//why are we forcing release self ??
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
				g.(surface, surfaceKey, args ++ [ [\output, [\bus, busses] ] ] )
			}
			{ Error("PSurface renderMethod unknown : %.\nHas to be either \vbap, \vbapTest or \direct".format(surface.renderMethod)).throw }
		}
		{\previewStereo}{
			//var points = ClusterArg( surface.pointsRV3D.collect{ |p| Point(p.x, p.y) } );
			//g.(surface, surfaceKey, args ++ [ [\stereoOutput, [\point, points ] ] ] )
			f.()
		}
		{ Error("ImmLib.mode unknown : %.\nHas to be either \normal or \previewStereo !".format(ImmLib.mode)).throw }

	}

	*newNoPanner { |surfaceKey...args|
		var surface = PSurfaceDef.get(surfaceKey);
		^this.doesNotUnderstand(*[\new]++this.prStripUMods(args) )
		.initImmUChain( args, this.prGetUmods(args), surface, surfaceKey )
	}

	initImmUChain { |args, inUMods, asurface, asurfaceKey|

        //connect each UMod with the corresponding ImmU
		var mus = this.doesNotUnderstand(\units).items
		.flop.collect({ |xs| ImmMU.fromArray(xs).surface_(asurface) });
        mods = [inUMods, mus].flopWith{ |uModOption,mu|
            uModOption.collect{ |uMod| uMod.asUModFor(mu) }
        };

		//"mods : %".format(mods).s;
		freeController = SimpleController(items.first);
		freeController.put(\end, {
			//"MUChain end % %".format(this.asString, this.hash ).postln;
			mods.catOptions.do(_.stop);
		});
		storeArgs = args;
		surface = asurface;
		surfaceKey = asurfaceKey;
    }

	getInitArgs {
		var numPreArgs = -1;
		var unitStoreArgs;

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
			//[U]                 //~[ [(key,val)] ]
			var args = unitArray.collect{ |x| x.args.clump(2) }
			.flop //[ [(key1,val)], [(key2,val)] ]
			.collect{ |uargArray|
				    //[values] for key i and for all units
				var values = uargArray.flop.at(1);
				if( (values.as(Set).as(Array).size == 1) ) {
					uargArray[0]
				} {
					[uargArray[0][0], values.carg]
				}
			}.select({ |item, i|
				/*(item[1].class != ClusterArg) &&*/
				(item != defArgs[i]) and:
				{ unit.dontStoreArgNames.includes( item[0] ).not };
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
					//[item[0],item[1].asFloat.asStringPrec(6).interpret]
				};
			}).flatten(1);

			if(mod.isDefined){
				[def, args, mod.get]
			} {
				[def, args]
			};
		};

		^([ surfaceKey, this.startTime, this.track, this.duration, this.releaseSelf ][..(numPreArgs+1)]) ++
		//[[units1],[units2],...] :: [[U]]
		[items.collect(_.units).flop, mods, (1..mods.size)-1]
		.flopWith( unitStoreArgs )
		//very hacky !
		.select{ |xs| ['pannerout','output','stereoOutput'].includes(xs[0]).not }
		.collect{ |xs|
			var noArgs = xs[1].size == 0;
			var noMod = xs[2].isNil;
			if( noArgs && noMod ) {
				//print just name
				xs[0]
			}{
				//print all
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
		if( items[0].fadeInCurve != 0.0 ) {
			stream << ".fadeInCurve_(" <<< items[0].fadeInCurve << ")";
		};
		if( items[0].fadeOutCurve != 0.0 ) {
			stream << ".fadeOutCurve_(" <<< items[0].fadeOutCurve << ")";
		};
		if( items[0].gain != 0.0 ) {
			stream << ".gain_(" <<< items[0].gain << ")";
		};

	}

	modAt_{ |i,mod|
		var check = this.checkArgs(\MUChain,\modAt_,[i,mod],[Integer, UEvNetTMod]);
		var xs = this.items.collect({ |x| x.units[i] });
		var mu = ImmMU.fromArray(xs).surface_(surface);
		var oldmod = mods[i];
		var t = (oldmod >>= { |x|
			if( x.isKindOf(UEvNetTMod) and: { x.playing } ) {
				Some( x.timer.t )
			} { None() }
		});
		oldmod.do{ |x|
			x.disconnect
		};
		mod.asUModFor(mu);
		t.postln.do{ |t|
			mod.start(nil, t)
		};
		mods[i] = Some(mod);
	}

}
		