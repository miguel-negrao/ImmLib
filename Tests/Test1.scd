//Test UChain with modulation
(
f = { |t|
    var freq = t.collect{ |t| sin(2*pi*t*0.1)  }.linlin(-1.0,1.0, 400, 800);
    Do(
        freq.debug("freq");
        return ( freq: UArg(freq) )
    )
};
x = UEvNetTModDef(f, 0.1);
y = UChain( [\sine, [\freq, 400], x], \stereoOutput);

y.gui;
y.start
)

//Test MUChain no modulation
(
y = MUChain( [\sine, [\freq, ClusterArg([400,800])]], \stereoOutput);

y.gui;
y.start
)

//Test MU with modulation
(
f = { |t|
    var freq = t.collect{ |t|
		[
			sin(2*pi*t*0.1).linlin(-1.0,1.0, 400, 800),
			sin(2*pi*t*0.2).linlin(-1.0,1.0, 100, 200)
		]
	};
    Do(
        freq.debug("freq");
        return ( freq: UArg(freq) )
    )
};
x = MUEvNetModDef(f, 0.1);
y = MU(\sine, [\freq, ClusterArg([400,800])], x);

y.gui;
y.start
)

//Test MUChain with modulation
(
f = { |t|
    var freq = t.collect{ |t|
		[
			sin(2*pi*t*0.1).linlin(-1.0,1.0, 400, 800),
			sin(2*pi*t*0.2).linlin(-1.0,1.0, 100, 200)
		]
	};
    Do(
        freq.debug("freq");
        return ( freq: UArg(freq) )
    )
};
x = MUEvNetModDef(f, 0.1);
y = MUChain([\sine, [\freq, ClusterArg([400,800])], x], [\stereoOutput, [\point,ClusterArg( Point(_,0.0) <%> [-1.0,1.0] )]] );

y.gui;
y.start
)

//Test sphere lib

(
Udef(\mynoise, {
	UOut.ar(0, BPF.ar(WhiteNoise.ar * 0.1, \freq.ar(440) ) * \amp.kr(1,1.0) )
})
)
{ WhiteNoise.ar * 0.1 }.play
(
~radial2 = PField({ |p, t, k|
	var tau = 2*pi;
	sin ( (p.phi*k) + t )
});

m = 2;

//surface definition
~surface = ParameterSurface.sphere(m);
~psPanners = ParameterSurfacePanners(~surface);

~timeSlider = LayoutSlider("time rate");
~kSlider = LayoutSlider("k");

~timeSlider[1].value_(0.3);
~kSlider[1].value_(0.5);

~descFunc = { |t|
    Do(
        trateSig <- ~timeSlider[1].asENInput;
        ksig <- ~kSlider[1].asENInput;
        let localtime = trateSig.linlin(0.0,1.0,0.0,6.0).integral(t);
        let x = ~radial2.( ~surface, localtime, ksig.linlin(0.0,1.0,0.0,6.0) );
		x.debug("values");
        return ( \amp : USpecArg( x ) )
    )
};

//synthesis definitions
~chain1 = MUChain(0,0,50,false,
    [\mynoise, nil , MUEvNetModDef(~descFunc, 0.1)],
	~psPanners.sendToPannersU
);

~pannerMU = ~psPanners.makePanners;

p = ~psPanners;

//GUI
w = Window().layout_(
    VLayout(*[ ~timeSlider[0], ~kSlider[0]])
);

//IO
w.front;
//~pannerMU.prepareAndStart( ~psPanners.groupsForPanners);
//~chain1.prepareAndStart(  ~psPanners.groupsForUnits );
~chain1.gui;
v = USession(*[
	MUChain.fromArray( ~pannerMU.items.collect{ |x| UChain(x) } ).postln.ugroup_('group1'),
	~chain1.ugroup_('group2'),
]);

v.prepareAndStart
)


x = { ReplaceOut.ar(0, In.ar(0,32).sum.dup ) }.play(s, addAction: \addToTail)

Server.scsynth


























ULib.servers = [s]
y.release

//last messages to go through after release
[ 0.2, [ 15, 1001, freq, 754.10264855516 ] ]
[ 0.2, [ 15, 1004, freq, 100.88563746357 ] ]
[ 11, 1000 ]
[ 11, 1003 ]

x = { Out.ar( 0, SinOsc.ar(\freq.ar(100) ) * 0.1 )}.play
(
10.do{ x.set(\freq,200) };
x.free
)

Server.default = s = Server.new('local-debug', DebugNetAddr("localhost", 57110));
s.boot

y.gui.front

y.items[1].gui