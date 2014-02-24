//PField.wave2D
(
q = ();
q.m = 20;
q.surface = PSurface.sphere(q.m);

q.mod = ImmDef({ |t, u0, v0, l, freq|

	var g = { |x| ( sin(2pi*x) + sin(2pi*4*x) ).tanh };
	var out = PField.wave2D(t, u0, v0, l, freq, g, true);

	(amp: USpecArg( out ) )
}, q.surface, 0.1, [ "u", [ 0, 6.2831853071796 ], "v", [ -3.1415926535898, 3.1415926535898 ], "l", [ 0, 2 ], "f", [ 0.1, 5 ] ]
 );

q.chain = MUChain.immNew(q.surface,
	[
		\immWhiteNoise,nil,
		 q.mod
	]
).fadeIn_(1).fadeOut_(1);

q.score = UScore.immNew(q.surface, q.chain);
q.scoregui = q.score.gui;
q.scoregui.askForSave = false;
q.score.prepareAndStart;
CmdPeriod.add({ w.close; q.scoregui.close; });
)

//PField.wave2DSin
(
q = ();
q.m = 20;
q.surface = PSurface.sphere(q.m);

q.mod = ImmDef({ |t, u0, v0, l, freq|

	var out = PField.wave2DSin(t, u0, v0, l, freq, true);

	(amp: USpecArg( out ) )
}, q.surface, 0.1,
[ "u", [ 0, 2pi ], "v", [ -pi/2, pi/2 ], "l", [ 0, 2 ], "f", [ 0.1, 5 ] ]
 );

q.chain = MUChain.immNew(q.surface,
	[
		\immWhiteNoise,nil,
		 q.mod
	]
).fadeIn_(1).fadeOut_(1);

q.score = UScore.immNew(q.surface, q.chain);
q.scoregui = q.score.gui;
q.scoregui.askForSave = false;
q.score.prepareAndStart;
CmdPeriod.add({ w.close; q.scoregui.close; });
)

//PField.wave2DSaw
(
q = ();
q.m = 20;
q.surface = PSurface.sphere(q.m);

q.mod = ImmDef({ |t, u0, v0, l, freq|

	var out = PField.wave2DSaw(t, u0, v0, l, freq, true);

	(amp: USpecArg( out ) )
}, q.surface, 0.1,
[ "u", [ 0, 2pi ], "v", [ -pi/2, pi/2 ], "l", [ 0, 2 ], "f", [ 0.1, 5 ] ]
 );

q.chain = MUChain.immNew(q.surface,
	[
		\immWhiteNoise,nil,
		 q.mod
	]
).fadeIn_(1).fadeOut_(1);

q.score = UScore.immNew(q.surface, q.chain);
q.scoregui = q.score.gui;
q.scoregui.askForSave = false;
q.score.prepareAndStart;
CmdPeriod.add({ w.close; q.scoregui.close; });
)

#METHOD::wave2DSaw (t, u0, v0, l, f, plot: false)

METHOD::wave2DSin (t, u0, v0, l, f, plot: false)

METHOD::waveUSin (t, l, f, plot: false)

METHOD::waveVSin (t, l, f, plot: false)