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

/*

types or arguments
1 [\freq, 400]
2 [\freq, X[200, 400] ]
3 [\fadeTimes, [10,20] ]
4  [\fadeTimes, X[ [10,20],[20,30] ] ]
How to distinguish 2 from 3 ? Using X[ ] instead of [ ]

Lets start just with arrays of array args, and then work array expansion.






*/
/*
MU : ClusterBasic {

/*
def, args
n, def, args

*/
/*
*new { |arg1, arg2, arg3|
var clusterObject = if( arg1.class == Symbol) {
super.newExpandCollect(\new, [arg1, arg2])
} {
super.newExpandCollect(\new, [arg2, arg3], Cluster( nil.dup(arg1) ) )
};
clusterObject.items.do{ |u,i|
u.setAudioIn(0,i).setAudioOut(0,i)
};
^clusterObject
}
*/
*oclass{ ^U }

apxCPU { |target|
^this.prExpandCollect(\apxCPU,[target]).items.sum
}
}
*/

ImmMU : MU {

	var <>surface;

}

MU : ClusterBasic {
    var <muDef;
    var <muArgs;
	var <mod;


    *oclass{ ^U }

    *new { |def, args, mod|
        ^this.doesNotUnderstand(\new, def, args).init(def, args, mod)
    }

	*fromArrayWithMod { |array, mod|
		^super.fromArray(array).initfromArrayWithMod(mod)
	}

	//no mod initialization here
	initfromArrayWithMod { |modArg|
		mod = modArg;
	}

    init { |def, args, inMod|
        muDef = def;
        muArgs = args;
		mod = inMod.asUModFor( this );
    }

    gui { |munit|
        var w = Window.new;
        w.addFlowLayout;
        items.collect(_.gui(w));
        ^w
    }

    start { |target, startPos = 0, latency|
		this.modPerform( \start, startPos, latency );
        ^this.doesNotUnderstand(\start,target,startPos, latency);
    }

    /*startIO { |target, startPos = 0, latency|
        ^IO{ this.doesNotUnderstand(\start,target,startPos, latency) } >>= { |return|
            uInteraction.collect( _.startIO ).getOrElseDoNothing.collect{ return }
        }
    }*/

	//don't need prepare method, UEvNet doesn't use it

    prepareAndStart { |target|
        this.modPerform( \start, 0, 0.2 );
        this.doesNotUnderstand(\prepareAndStart, target);
    }

    /*prepareAndStartIO { |target|
        ^IO{ this.doesNotUnderstand(\prepareAndStart, target) } >>= { |return|
            uInteraction.collect( _.startIO ).getOrElseDoNothing.collect{ return }
        }
    }*/

    free {
        this.doesNotUnderstand(\free);
		this.modPerform( \stop );
    }

    /*freeIO {
        ^IO{ this.doesNotUnderstand(\free) } >>=| uInteraction.stopIO
    }*/

    dispose {
        this.doesNotUnderstand(\free);
        this.modPerform( \dispose );
    }

	disposeArgsFor { |server|
		this.doesNotUnderstand(\disposeArgsFor, server);
		this.modPerform( \disposeArgsFor );
	}

    /*disposeIO {
        ^IO{ this.doesNotUnderstand(\free) } >>=|
        eventNetwork.collect( _.pause ).getOrElse( Unit.pure(IO) )
    }*/

	def{
		^items[0].def
	}

	defName {
		^items[0].defName
	}

    storeArgs {
        ^[muDef, muArgs, mod]
    }

	modPerform { |what ...args| mod !? _.perform( what, this, *args ); }
}

MUChain : ClusterBasic {

    var storeArgs; // [symbol, argValueList, UInteraction]
    var <mods; //[ UInteraction ]
    var <eventNetwork; // Option[ EventNetwork ]
	var <>private = false;
	var <freeController;
    *oclass{ ^UChain }


    *new { arg ... args;
        ^this.doesNotUnderstand(*[\new]++this.prStripUMods(args) )
		.init( args, this.prGetUmods(args) )
    }

	*prStripUMods { |args|
		//need to extract the umods from the args.
        //sorry about the lack of pattern matching...
		//version of arguments without umods
		//could have been done with higher order functions, but well...
        var stripUMods = { |xs| xs.match(LazyListEmpty.constf,{ |head,tail|
            if( head.isNumber || head.isKindOf(Boolean) ) {
                head %% stripUMods.(tail)
            } {
                if( head.isArray ) {
					//removing the mod here
            LazyListCons( head[..1], stripUMods.(tail) )
                } {
                    head %% stripUMods.(tail)
                }
            }
        } ) };
		^stripUMods.(args.asLazy).asArray
	}

	*prGetUmods { |args|
		//get rid of initial track number, duration, etc.
        var stripStart = { |xs| xs.match(LazyListEmpty.constf,{ |head,tail|
            if( head.isNumber || head.isKindOf(Boolean) ) {
                stripStart.(tail)
            } {
                xs
            }
        } ) };
		//version of arguments just with umods
		//returns Array[Option[MUEvNetModDef]]
        var getUMods = { |xs| stripStart.(xs).match(LazyListEmpty.constf,{ |head,tail|
            if( head.isArray ) {
                LazyListCons( head[2].asOption, getUMods.(tail) )
            } {
				None()  %% getUMods.(tail)
            }
        } ) };
        ^getUMods.(args.asLazy).asArray;
	}

    init { |args, inUMods|

        //connect each UMod with the corresponding MU
		var mus = this.doesNotUnderstand(\units).items.flop.collect( MU.fromArray(_) );
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
    }

	modAt_{ |i,mod|
		var check = this.checkArgs(\MUChain,\modAt_,[i,mod],[Integer, UEvNetTMod]);
		var mu = MU.fromArray(this.items.collect({ |x| x.units[i] }) );
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

    //temporary fix ?
    units {
		^[this.doesNotUnderstand(\units).items.flop, mods].flopWith{ |xs,modOption|
			MU.fromArrayWithMod(xs, modOption.orNil)
		}
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

			var args = unitArray.collect{ |x| x.args.clump(2) }.flop.collect{ |uargArray|
				var values = uargArray.flop.at(1);
				if( (values.as(Set).as(Array).size == 1) ) {
					uargArray[0]
				} {
					[uargArray[0][0], values.carg]
				}
			}.select({ |item, i|
				(item != defArgs[i]) && { unit.dontStoreArgNames.includes( item[0] ).not };
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

		^([ this.startTime, this.track, this.duration, this.releaseSelf ][..numPreArgs]) ++
		[items.collect(_.units).flop, mods, (1..mods.size)-1].flopWith( unitStoreArgs )
		.select{ |xs| xs[0] != 'pannerout' }
		.collect{ |xs|
			if(xs[1].size == 0) {
				xs[0]
			}{
				xs
			}
		}
	}

	storeArgs { ^this.getInitArgs }

	storeModifiersOn { |stream|
		items[0].storeTags( stream );
		items[0].storeDisplayColor( stream );
		items[0].storeDisabledStateOn( stream );
		if( items[0].ugroup.notNil ) {
			stream << ".ugroup_(" <<< this.ugroup << ")";
		};
		if( items[0].serverName.notNil ) {
			stream << ".serverName_(" <<< items[0].serverName << ")";
		};
		if( items[0].addAction != \addToHead ) {
			stream << ".addAction_(" <<< items[0].addAction << ")";
		};
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

	collectOSCBundleFuncs { |server, startOffset = 0, infdur = 60|
		^this.doesNotUnderstand(\collectOSCBundleFuncs, server, startOffset, infdur).items.reduce('++').postln
	}

    prStartBasic { |target, startPos = 0, latency, withRelease = false|
		mods.catOptions.do(_.start(nil, startPos) );
        ^this.doesNotUnderstand(\prStartBasic, target, startPos, latency, withRelease)
    }

    prStartBasicIO { |target, startPos = 0, latency, withRelease = false|
        ^mods.catOptions.collect(_.startIO).sequece >>=| IO{ this.doesNotUnderstand(\prStartBasic, target, startPos, latency, withRelease) }
    }

    start { |target, startPos = 0, latency|
        this.prStartBasic(target, startPos, latency, false )
    }

    startIO { |target, startPos = 0, latency|
        ^this.prStartBasicIO(target, startPos, latency, false )
    }

    startAndRelease { |target, startPos = 0, latency|
        this.prStartBasic(target, startPos, latency, true )
    }

    startAndReleaseIO { |target, startPos = 0, latency|
        ^this.prStartBasicIO(target, startPos, latency, true )
    }

    prepareAndStart { |...args|
        mods.catOptions.do(_.start);
        this.doesNotUnderstand(*([\prepareAndStart]++args));
    }

    prepareAndStartIO { |...args|
        ^mods.catOptions.collect( _.startIO ).sequence >>=|
        IO{ this.items.collect{ |x| x.units[0].args } } >>=|
        IO{ this.doesNotUnderstand(*([\prepareAndStart]++args)) }
    }

	prepare { |target, startPos = 0, action|
		var multiAction = MultiActionFunc( {
			    action.value;
		} );
		var actions = ClusterArg(items.collect{ multiAction.getAction });

		^this.doesNotUnderstand(\prepare, target, startPos, actions)
	}

    stop {
		mods.catOptions.do(_.stop);
        this.doesNotUnderstand(\stop);
    }

	free {
		mods.catOptions.do(_.stop);
        this.doesNotUnderstand(\free);
    }

    stopIO {
        ^IO{ this.doesNotUnderstand(\stop) } >>=|
        mods.catOptions.collect( _.stopIO ).sequence
    }

    release { |time|
		mods.catOptions.do(_.stop);
        this.doesNotUnderstand(\stop, time);
    }

    releaseIO { |time|
        ^IO{ this.doesNotUnderstand(\stop, time) } >>=|
        mods.catOptions.collect( _.stopIO ).sequence
    }

    dispose {
        this.doesNotUnderstand(\dispose);
		mods.catOptions.do(_.dispose)
    }

	disconnect {
		this.doesNotUnderstand(\disconnect);
		mods.catOptions.do(_.disconnect)
	}

    disposeIO {
        ^IO{ this.doesNotUnderstand(\dispose) } >>=|
        eventNetwork.collect( _.pause ).getOrElseDoNothing
    }

    gui {
        ^ImmMassUChain( this.items, mods ).gui;
    }

    guiIO {
        ^IO{ this.gui }
    }

    //methods for which we shouldn't return a ClusterArg

	canFreeSynth {
        ^items[0].canFreeSynth
    }

    releaseSelf {
        ^items[0].releaseSelf
    }

    eventEndTime {
        ^items[0].eventEndTime
    }

    startTime {
        ^items[0].startTime
    }

    prepareTime {
        ^items[0].startTime
    }

    isFolder {
        ^items[0].isFolder
    }

    disabled {
        ^items[0].disabled
    }

    dur {
        ^items[0].dur
    }

    duration {
        ^items[0].duration
    }

    endTime {
        ^items[0].endTime
    }

	finiteDuration {
		^items[0].finiteDuration
	}

	getGain {
		^items[0].getGain
	}

	muted {
		^items[0].muted
	}

    <= { |b|
        ^items[0] <= b.items[0]
    }

    track {
        ^items[0].track
    }

	makeView{ |i=0,minWidth, maxWidth| ^UChainEventView(this, i, minWidth, maxWidth) }

	addDependant { arg dependant;
		items[0].addDependant(dependant);
	}

	getTypeColor {
		^items[0].getTypeColor
    }

	fadeTimes {
		^items[0].fadeTimes
    }

	fadeIn {
		^items[0].fadeIn
    }

	fadeOut {
		^items[0].fadeOut
    }


	fadeInCurve {
		^items[0].fadeInCurve
    }

	fadeOutCurve {
		^items[0].fadeOutCurve
    }


	name {
		^"M "++items[0].name
    }

	hideInGUI {
		^items[0].hideInGUI
	}

	duplicate{
	    ^this.cs.interpret;
	}

	lockStartTime {
		^items[0].lockStartTime
	}

}