

PFFuncs {

    //{ |x| PFFuncs.bump(x) }.plotGraph
    *bump { |x| ^2**((1-x.squared).reciprocal.neg)*2 }


    *geodesicDist { |theta1,phi1|
        ^{ |theta2,phi2|
            acos( cos(phi1)*cos(phi2)*cos(theta1-theta2) + (sin(phi1)*sin(phi2) ) )
        }
    }

    *geodesicDist2 { |theta1, phi1, theta2, phi2|
        ^acos( cos(phi1)*cos(phi2)*cos(theta1-theta2) + (sin(phi1)*sin(phi2) ) )
    }

    *growArea { |startPoint|
        ^{ |uspherical, c, d|
            var dist = this.geodesicDist2(startPoint.theta, startPoint.phi, uspherical.theta, uspherical.phi);
            var c2 = c.linlin(0.0,1.0, d.neg, 1.0);
            var cpi = c2*pi; //half of the perimeter of a unit circle measures pi
            var cpid = cpi+d;
            if( dist < cpi ) {
                1
            } {
                if( dist < cpid ) {
                    this.bump( dist.linlin(cpi,cpid,0.0,1.0) )
                } {
                    0
                }
            }
        }
    }

    *testAudio{ |surface, udef, specs, descFunc, delta = 0.1, plots|

        //surface definition
        var p = ParameterSurfacePanners(surface);

        var wAndDesc = if( specs.size > 0 ) {
            var x = UAnimatedInteraction.makeSLWinAndDescFunc(specs, descFunc);
            x.at1_( Some(x.at1) )
        } {
            T(None,descFunc)
        };

        //synthesis definitions
        var chain = MUChain(
            [udef, nil , MUAnimatedInteraction( wAndDesc.at2, delta )],
            p.sendToPannersU
        );

        var panner = p.makePanners;

        ^wAndDesc.at1
        .collect{ |w| IO{ w.front; CmdPeriod.doOnce({ w.close }) } }
        .getOrElse( Unit.pure(IO) ) >>=|
        plots.collect( _.startRendererIO ).sequence(IO) >>=|
        panner.prepareAndStartIO( p.groupsForPanners ) >>=|
        chain.prepareAndStartIO(  p.groupsForUnits )
    }

    //broken
     *testAudio2{ |udef, specs, descFunc, delta = 0.1, plots|
        ^{ |panners|
            var wAndDesc = if( specs.size > 0 ) {
                var x = UAnimatedInteraction.makeSLWinAndDescFunc(specs, descFunc);
                x.at1_( Some(x.at1) )
            } {
                T(None,descFunc)
            };

            //synthesis definitions
            var chain = MUChain(
                [udef, nil , MUAnimatedInteraction( wAndDesc.at2, delta )],
                panners.sendToPannersU
            );

            var windowIO = wAndDesc.at1
            .collect{ |w| IO{ w.front; CmdPeriod.doOnce({ w.close }) } }
            .getOrElse( Unit.pure(IO) );

            var plotIO = plots.collect( _.startRendererIO ).sequence(IO);

            var chainIO = chain.prepareAndStartIO( panners.groupsForUnits ).fmap{ panners };

            ^windowIO >>=| plotIO >>=| chainIO
        }
    }

    *testAudioMakePanners { |surface|
        var p = ParameterSurfacePanners(surface);
        ^p.makePanners.prepareAndStartIO( p.groupsForPanners ).fmap{ p }
    }

    /*
    transforms a function of p,t,a1,a2,.. into a function of
    p,t,angle1,angle2,a1,a2,a3... whih rotates the points before applying the function
    around the vector V(1.0,0.0,0.0) and V(0.0,1.0,0.0)s
    */
    *rotate{ |f| //angle1, 2, 3 -pi/2, pi/2
        ^{ |p,t,angle1,angle2...args|
            var newP = p.rotateXZ(angle1).rotateYZ(angle2);
            f.valueArray( [newP,t]++args )
        }
    }

}