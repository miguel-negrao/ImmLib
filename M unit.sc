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
    var <uInteraction;
    var <eventNetwork;

    *oclass{ ^U }

    *new { |def, args, inuInteraction|
        ^this.doesNotUnderstand(\new, def, args).init(def, args, inuInteraction)
    }

    init { |def, args, inuInteraction|
        muDef = def;
        muArgs = args;
        uInteraction = inuInteraction;
        //eventNetwork = None;
        eventNetwork = uInteraction.asOption.collect{ |x| EventNetwork( x.createDesc(this) ) };
        eventNetwork.do( _.actuateNow );
    }

    gui { |munit|
        var w = Window.new;
        w.addFlowLayout;
        items.collect(_.gui(w));
        ^w
    }

    start { |target, startPos = 0, latency|
        var return = ClusterArg(items.collect(_.start));
        uInteraction.start;
        ^return
    }

    free {
        this.synths.do(_.free);
//////
        uInteraction.stop;
//////
    }


    // not used anymore ?
    map { |key, es|
        ^es.collect{ |v|
            IO{ this.set(key, ClusterArg(v)) }
        }
    }

    mapWithSpec { |key,es|
        ^es.collect{ |v|
            IO{ this.mapSet(key, ClusterArg(v)) }
        }
    }

    storeArgs {
        ^[muDef, muArgs, uInteraction]
    }
}

MUChain : ClusterBasic {

    var <storeArgs; // [symbol, argValueList, UInteraction]
    var <uInteractions; //[ Option[ UInteraction ] ]
    var <eventNetwork; // Options[ EventNetwork ]
    *oclass{ ^UChain }


    *new { arg ... args;
        //need to extract the uInteractions from the args.
        //sorry about the lack of pattern matching...
        var stripUInteractions = { |xs| xs.match(LazyListEmpty.constf,{ |head,tail|
            if( head.isNumber || head.isKindOf(Boolean) ) {
                head %% stripUInteractions.(tail)
            } {
                if( head.isArray ) {
            LazyListCons( head[..1], stripUInteractions.(tail) )
                } {
                    head %% stripUInteractions.(tail)
                }
            }
        } ) };
        var stripStart = { |xs| xs.match(LazyListEmpty.constf,{ |head,tail|
            if( head.isNumber || head.isKindOf(Boolean) ) {
                stripStart.(tail)
            } {
                xs
            }
        } ) };
        var getUInteractions = { |xs| stripStart.(xs).match(LazyListEmpty.constf,{ |head,tail|
            if( head.isArray ) {
                LazyListCons( head[2].asOption, getUInteractions.(tail) )
            } {
                None  %% getUInteractions.(tail)
            }
        } ) };
        var uInteractions = getUInteractions.(args.asLazy).asArray;
        ^this.doesNotUnderstand(*[\new]++stripUInteractions.(args.asLazy).asArray)
        .init( args, uInteractions )
    }

    init { |args, auInteractions|
        storeArgs = args;

        //connect each UInteraction with the corresponding MU
        eventNetwork = [auInteractions, this.units].flopWith{ |uIntOption,mu|
            uIntOption.collect{ |uInt| uInt.createDesc(mu) }
        }.catOptions.reduce(_ >>=| _ ).asOption.collect( EventNetwork(_) );

        uInteractions = auInteractions.catOptions;

        eventNetwork.do( _.actuateNow );
    }

    //temporary fix ?
    units {
        ^super.units.items.flop.collect( MU.fromArray(_) )
    }

    prStartBasic { |target, startPos = 0, latency, withRelease = false|
        var return  = ClusterArg(items.collect( _.prStartBasic(target, startPos, latency, withRelease) ));
        uInteractions.do(_.start);
        ^return
    }

    start { |target, startPos = 0, latency|
        this.prStartBasic(target, startPos, latency, false )
    }

    startAndRelease { |target, startPos = 0, latency|
        this.prStartBasic(target, startPos, latency, true )
    }

    prepareAndStart { |...args|
        items.do(_.prepareAndStart(*args))
    }

    stop {
        items.do(_.stop);
        uInteractions.do(_.stop);
    }

    release { |time|
        items.do(_.release( time ) ) ;
        uInteractions.do(_.stop);
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

    <= { |b|
        ^items[0] <= b.items[0]
    }

    track {
        ^items[0].track
    }

}