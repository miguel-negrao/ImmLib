/*
Unit Library
The Game Of Life Foundation. http://gameoflife.nl
Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

GameOfLife Unit Library: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GameOfLife Unit Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.


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

MU : ClusterBasic {
    var <muDef;
    var <muArgs;
	var <mod;


    *oclass{ ^U }

    *new { |def, args, mod|
        ^this.doesNotUnderstand(\new, def, args).init(def, args, mod)
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
        ^IO{ this.doesNotUnderstand(\prepareAndStart, target).postln } >>= { |return|
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

    storeArgs {
        ^[muDef, muArgs, mod]
    }

	modPerform { |what ...args| mod !? _.perform( what, this, *args ); }
}

MUChain : ClusterBasic {

    var <storeArgs; // [symbol, argValueList, UInteraction]
    var <mods; //[ UInteraction ]
    var <eventNetwork; // Option[ EventNetwork ]
    *oclass{ ^UChain }


    *new { arg ... args;
        ^this.doesNotUnderstand(*[\new]++this.prStripUMods(args) )
		.init( args, this.prGetUmods(args) )
    }

	*prStripUMods { |args|
		//need to extract the umods from the args.
        //sorry about the lack of pattern matching...
		//version of arguments without umods
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
        mods = [inUMods, this.units].flopWith{ |uModOption,mu|
            uModOption.collect{ |uMod| uMod.asUModFor(mu) }
        }.catOptions;

		"mods : %".format(mods).postln;

		storeArgs = args;
    }

    //temporary fix ?
    units {
        ^super.units.items.flop.collect( MU.fromArray(_) )
    }

    prStartBasic { |target, startPos = 0, latency, withRelease = false|
        mods.do(_.start);
        ^this.doesNotUnderstand(\prStartBasic, target, startPos, latency, withRelease)
    }

    prStartBasicIO { |target, startPos = 0, latency, withRelease = false|
        ^mods.collect(_.startIO).sequece >>=| IO{ this.doesNotUnderstand(\prStartBasic, target, startPos, latency, withRelease) }
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
        mods.do(_.start);
        this.doesNotUnderstand(*([\prepareAndStart]++args));
    }

    prepareAndStartIO { |...args|
        ^mods.collect( _.startIO ).sequence >>=|
        IO{ this.items.collect{ |x| x.units[0].args }.postln } >>=|
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
		mods.do(_.stop);
        this.doesNotUnderstand(\stop);
    }

	free {
		mods.do(_.stop);
        this.doesNotUnderstand(\free);
    }

    stopIO {
        ^IO{ this.doesNotUnderstand(\stop) } >>=|
        mods.collect( _.stopIO ).sequence
    }

    release { |time|
		mods.do(_.stop);
        this.doesNotUnderstand(\stop, time);
    }

    releaseIO { |time|
        ^IO{ this.doesNotUnderstand(\stop, time) } >>=|
        mods.collect( _.stopIO ).sequence
    }

    dispose {
        this.doesNotUnderstand(\dispose);
		mods.do(_.dispose)
    }

    disposeIO {
        ^IO{ this.doesNotUnderstand(\dispose) } >>=|
        eventNetwork.collect( _.pause ).getOrElseDoNothing
    }

    gui {
        ^MassEditUChain( this.items ).gui;
    }

    guiIO {
        ^IO{ this.gui }
    }

    //methods for which we shouldn't return a ClusterArg
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

	name {
		^"M "++items[0].name
    }

	hideInGUI {
		^items[0].hideInGUI
	}

	duplicate{
	    ^this.deepCopy;
	}

}