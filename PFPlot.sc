//unused
ParameterFieldPlotOld1 {

	*new { |psurface, arrayES|

		var w, width = 500, height = 400, rate = 0.005, size = 0.05, points, items, func, u;

		w !? _.close;
		w = Window("SphereSphape 1", Rect(128, 64, width, height+35), false).front;
		w.addFlowLayout;


		items = psurface.points.collect{ |p|
			var xzy = p.asCartesian;

			Canvas3DItem.cube
				.transform(Canvas3D.mScale(*size.dup(3)))
				.transform(Canvas3D.mTranslate(xzy.x,xzy.y,xzy.z))

		};

		u = Canvas3D(w, Rect(0, 0, width, height), items++[Canvas3DItem.cube])
			.scale_(200)
			.perspective_(0.3)
			.distance_(2);

		u.mouseMoveAction = {|v,x,y|
			u.transforms = [
				Canvas3D.mRotateY(x / -200 % 2pi),
				Canvas3D.mRotateX(y / 200 % 2pi)
			];
			u.refresh;
		};

		func = { |arr|
			[items,arr].flopWith{ |item, v|
				item.color_(Color.green(v));
			};
			defer{ u.refresh };
		};

		arrayES.do(func);

		w.onClose_{
			arrayES.stopDoing(func)
		}
	}

}

ParameterFieldPlotOld2 {
	var <items, <u, <w, <es;
	*new { |psurface| //EventSource[Array[Double]]

		var w, width = 500, height = 400, rate = 0.005, size = 0.05, points, items, u;

		w = Window("SphereSphape 1", Rect(128, 64, width, height+35), false);
		w.addFlowLayout;


		items = psurface.points.collect{ |p|
			var xzy = p.asCartesian;

			Canvas3DItem.cube
				.transform(Canvas3D.mScale(*size.dup(3)))
				.transform(Canvas3D.mTranslate(xzy.x,xzy.y,xzy.z))

		};

		u = Canvas3D(w, Rect(0, 0, width, height))
			.scale_(200)
			.perspective_(0.3)
			.distance_(2);

		u.items = items ++ [Canvas3DItem.cube];

		u.transforms = [ Canvas3D.mRotateX(pi/2) ];

		u.mouseMoveAction = {|v,x,y|
			u.transforms = [
				Canvas3D.mRotateY(x / -200 % 2pi),
				Canvas3D.mRotateX(y / 200 % 2pi)
			];
			u.refresh;
		};
		^super.newCopyArgs(items, u, w).init;
	}

	init {
		w.onClose_({ es.remove });
	}

	animate{ |arrayES|
		es = arrayES.collect{ |arr|
			IO{
				[items,arr].flopWith{ |item, v|
					item.color_(Color.green(v));
				};
				defer{ u.refresh };
			}
		};
		^es.reactimate
	}

	show {
		^IO{ w.front }
	}

	close {
		^IO{ w.isClosed.not.if({ w.close}) }
	}

	showNow {
		w.front
	}
}

HaskellPFPlot {
    classvar <currentPort = 30000,
    <binaryPath = "pfVisualizer";
    var <rendererAddr, <label="";

    *new{ |addr, label|
        currentPort = currentPort + 1;
        ^super.newCopyArgs(addr, label)
    }

	*enAnimate{ |...args|
		^ENDef.appendToResult( this.animate( *args ) );
	}

    startCommand {
		^HaskellPFPlot.binaryPath++" "++rendererAddr.port++" "++label;
    }

    startRenderer { |closeOnCmdPeriod = true|
        this.startCommand.unixCmd;
        if( closeOnCmdPeriod ) {
            CmdPeriod.doOnce({ this.stopRenderer })
        };
    }

    startRendererIO {
        ^this.startCommand.unixCmdIO
    }

    stopRenderer {
        rendererAddr.sendMsg("/quit");
    }

    stopRendererIO {
        ^rendererAddr.sendMsgIO("/quit")
    }

    *killall {
       this.killAllCommand.unixCmd
    }

    *killallIO {
        ^this.killAllCommand.unixCmdIO
    }

    *killAllCommand {
        ^"killall pfVisualizer"
    }

}


/*
x = ParameterFieldPlot2( \sphere, "test"  );
x.startRenderer
x.stopRenderer
*/
PFieldPlot : HaskellPFPlot {
    var <faces, <surface;

    *new { |type = \sphere, label|
        var faces = FP3DPoligon.sphereFaces(2);
        var points = faces.collect{ |vertices| vertices.sum / 3 };
        var surface = ParameterSurface( points.collect{ |p|
            //var p1 = p.asUnitSpherical;
            //UnitSpherical(p1.theta-(pi/2), p1.phi)
            p.asUnitSpherical
        } );
        currentPort = currentPort + 1;
        ^super.new( NetAddr("localhost", currentPort), label ).init( faces, surface )
    }

    *animate{ |pf...args|
        var plot = this.new;
        ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animate(pf, *args)
    }

	*animateOnly{ |pf...args|
        var plot = this.new;
        ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animateOnly(pf, *args)
    }

    init { |aFaces, aSurface|
        faces = aFaces;
        surface = aSurface;
    }

    startRenderer { |closeOnCmdPeriod = true|
        fork{
            super.startRenderer(closeOnCmdPeriod);
            1.wait;
            faces.clump(150).do{ |faces,i|
                rendererAddr.sendMsg(* ([if(i==0){"/triangles"}{"/add_triangles"}]++faces.flat))
            };
        }
    }

    startRendererIO {
        ^IO{ this.startRenderer }
    }

    animate{ |pf...args| //args surface, t, c1, c2, c3...
        var tEventSource = args[1].changes;
        var sendColors = { |v| IO{
            rendererAddr.sendMsg(* (["/colors"]++v.collect{ |v2|
                [0.0,v2.linlin(0.0,1.0,0.3,1.0),0.0]
            }.flat))
            }
        };
        var outSignal = pf.(*args);
        var outSignal2 = (sendColors.(_) <%> pf.( *([surface]++args[1..]) ) );
        ^(outSignal2  <@ tEventSource).reactimate.collect{ outSignal }
    }

    animateOnly { |pf...args| //args t, c1, c2, c3...
        var tEventSource = args[0].changes;
        var sendColors = { |v| IO{
            var msg = ["/colors"]++v.collect{ |v2|
                //set greem to corresponding intensity
                [0.0, v2.linlin(0.0,1.0,0.3,1.0), 0.0]
            }.flat;
            rendererAddr.sendMsg( *msg )
        } };
        ^( (sendColors.(_) <%> pf.value( *([surface]++args) )) <@ tEventSource).reactimate
    }

}


/*
x = ParameterGridPlot( ParameterSurface.geodesicSphere );
x.startRenderer
x.stopRenderer
*/
PGridPlot : HaskellPFPlot {
    var <points;

    *new { |surface, label|
        var points = surface.points.collect{ |x| x.asCartesian.asArray }.flat;
        ^super.new( NetAddr("localhost", currentPort), label ).init( points )
    }

    *animate{ |surface, sig, label|
        var plot = this.new(surface, label);
         ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animate(sig)

    }

    init { |aPoints|
        points = aPoints
    }

    startRenderer { |closeOnCmdPeriod = true|
        fork{
            super.startRenderer(closeOnCmdPeriod);

            1.wait;

            rendererAddr.sendMsg(* (["/cubes"]++points) )
        }
    }

    startRendererIO {
        ^IO{ this.startRenderer }
    }

    animate{ |sig|
        ^sig.collect{ |vs|
            IO{
                rendererAddr.sendMsg(*(["/colors"]++vs.collect{ |v|
                [0.0,v.linlin(0.0,1.0,0.3,1.0),0.0]
            }.flat) )
            }
        }.reactimate
    }
}

PFieldHemiPlot {

	*new{ |surface, sig|
		var values = surface.points.collect{ 1.0 };
		var points = surface.points.collect{ |p|
			var r = p.asRealVector3D;
			T( r.z < 0, Point(r.x, r.y) )
		};
		var w = Window("Dual Hemisphere Plot", Rect(100, 200, 700, 300));
		var p1 = (w.bounds.width/4)@(w.bounds.height/2);
		var p2 = (w.bounds.width/4*3)@(w.bounds.height/2);
		var radius = w.bounds.height/2;
		var p3 = Point(radius,radius);
		var centers = points.collect{ |t|
			if(t.at1) {
				p1 +
				(p3 * t.at2)
			} {
				p2 +
				(p3 * t.at2)
			}
		};
		w.view.background_(Color.white);
		w.drawFunc = { |v|
			Pen.color = Color.green(0.1,0.5);
			Pen.addWedge(
				p1,
				radius,
				0,
				2*pi
			);
			Pen.fill;
			Pen.addWedge(
				p2,
				radius,
				0,
				2*pi
			);
			Pen.fill;
			"South".drawAtPoint(Point(10,10),color:Color.black);
			"North".drawAtPoint(Point(w.bounds.width/2+10,10),color:Color.black);
			[centers, values ].flopWith{ |c, v|
				Pen.color = Color.yellow(v);
				Pen.addWedge(
					c,
					10,
					0,
					2*pi
				);
				Pen.fill;
			}


		};
		^Writer(Unit, T([],[],[IO{
			w.front;
			CmdPeriod.doOnce{ if(w.isClosed.not){ w.close } };
		}]) ) >>=|
		sig.collect{ |xs| IO{
			values = xs;
			{ w.refresh }.defer;
		} }.reactimate;
	}
}