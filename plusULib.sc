+ ULib {
    *serversFlat {
        ^servers.collect{ |s|
            if( s.isKindOf( LoadBalancer ) ) {
                s.servers
            } {
                s
            }
        }.flat
    }

    *waitForServersToBoot {
        while({ this.serversFlat.collect( _.serverRunning ).every( _ == true ).not; },
            { 0.2.wait; });
    }

    *sync {
        this.serversFlat.do( _.sync )
    }

    *sendDefs { |defs|
        this.serversFlat.do{ |s|
            defs.do{ |def|
                def.send(s)
            }
        }
    }

    *serversWindow {
        var makePlotTree, makeMeter, killer;
        var servers = ULib.serversFlat;
        var w = Window("ULib servers", Rect(10, 10, 380, 3 + ( servers.size * 29))).front;
        w.addFlowLayout;
        killer = Button(w, Rect(0,0, 20, 18));
        killer.states = [["K"]];
        killer.canFocus = false;
        killer.action = { Server.killAll };
        w.view.decorator.postln.nextLine;
        servers.do{ |s| s.makeView(w) };
        w.view.keyDownAction = { arg view, char, modifiers;
            // if any modifiers except shift key are pressed, skip action
            if(modifiers & 16515072 == 0) {

                case
                {char === $n } { servers.do( _.queryAllNodes(false) ) }
                {char === $N } { servers.do( _.queryAllNodes(true) ) }
                {char === $l } { makeMeter.() }
                {char === $p}  { makePlotTree.() }
                {char === $ }  { servers.do{ |s| if(s.serverRunning.not) { s.boot } } }
                /*{char === $s } { if( (this.isLocal and: (GUI.id == \qt)) or: ( this.inProcess ))
                {this.scope(options.numOutputBusChannels)}
                {warn("Scope not supported")}
                }
                {char === $f } { if( (this.isLocal and: (GUI.id == \qt)) or: ( this.inProcess ))
                {this.freqscope}
                {warn("FreqScope not supported")}
                }
                {char == $d } {
                if(this.isLocal or: { this.inProcess }) {
                if(dumping, stopDump, startDump)
                } {
                "cannot dump a remote server's messages".inform
                }
                }
                {char === $m } { if(this.volume.isMuted) { this.unmute } { this.mute } }
                {char === $0 and: {volumeNum.hasFocus.not}} {
                this.volume = 0.0;
                };*/
            };
        };
        makePlotTree = {
            var onClose, comp;
            var servers = ULib.serversFlat;
            var window = Window.new("Node Tree(s)",
                Rect(128, 64, 1000, 400),
                scroll:true
            ).front;
            var x = CompositeView(window.view, Rect(0,0,4000,4000));
            x.addFlowLayout(0@0,0@0);
            comp = servers.collect{ CompositeView(x,400@400) };
            window.view.hasHorizontalScroller_(false).background_(Color.grey(0.9));
            onClose = [servers, comp].flopWith{ |s,c| s.plotTreeView(0.5, c, { defer {window.close}; }) };
            window.onClose = {
                onClose.do( _.value );
            };
        };
        makeMeter = {
            var window = Window.new("Meter",
                Rect(128, 64, 1000, 1000),
            ).front;
            var x = CompositeView(window.view, Rect(0,0, 1000, 1000));
            x.addFlowLayout;
            servers.do{ |s|
                var numIns = s.options.numInputBusChannels;
                var numOuts = s.options.numOutputBusChannels;
                ServerMeterView(s, x, 0@0, numIns, numOuts)
            }
        };
        ^w
    }


}
