import { spriteUrl } from '../api'
import { useEffect, useRef } from 'react'
import * as THREE from 'three'
import { MODES, effectFor } from './typeEffects'

const MY_SIDE  = { x: -2.6, y: 1.15, z:  0.6, scale: 2.6 }
const OPP_SIDE = { x:  2.6, y: 1.55, z: -1.8, scale: 2.0 }

const MY_SIDE_X = MY_SIDE.x
const OPP_SIDE_X = OPP_SIDE.x

const EMITTER_COUNT = 4
const EMITTER_CAPACITY = 240


function makeHiddenTexture() {
    const canvas = document.createElement('canvas')
    canvas.width = canvas.height = 256
    const ctx = canvas.getContext('2d')

    ctx.fillStyle = '#1b2440'
    ctx.beginPath()
    ctx.roundRect(28, 28, 200, 200, 24)
    ctx.fill()
    ctx.strokeStyle = '#4b6bff'
    ctx.lineWidth = 6
    ctx.stroke()

    ctx.fillStyle = '#4b6bff'
    ctx.font = 'bold 120px system-ui, sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('?', 128, 132)

    const texture = new THREE.CanvasTexture(canvas)
    texture.colorSpace = THREE.SRGBColorSpace
    return texture
}

function makePlatform(color) {
    const group = new THREE.Group()

    const disc = new THREE.Mesh(
        new THREE.CylinderGeometry(1.25, 1.35, 0.18, 48),
        new THREE.MeshStandardMaterial({ color: 0x1a2138, roughness: 0.55, metalness: 0.3 })
    )
    group.add(disc)

    const ring = new THREE.Mesh(
        new THREE.TorusGeometry(1.3, 0.05, 12, 64),
        new THREE.MeshBasicMaterial({ color })
    )
    ring.rotation.x = Math.PI / 2
    ring.position.y = 0.12
    group.add(ring)
    group.userData.ring = ring

    return group
}


function makeEmitter() {
    const positions = new Float32Array(EMITTER_CAPACITY * 3)
    const colors = new Float32Array(EMITTER_CAPACITY * 3)
    positions.fill(-9999) // 안 쓰는 파티클은 화면 밖으로 치워 둔다

    const geometry = new THREE.BufferGeometry()
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

    const material = new THREE.PointsMaterial({
        size: 0.12,
        vertexColors: true,
        transparent: true,
        opacity: 0,
        depthWrite: false,
        blending: THREE.AdditiveBlending,
    })

    const points = new THREE.Points(geometry, material)
    points.frustumCulled = false
    points.userData = {
        positions,
        colors,
        velocities: new Float32Array(EMITTER_CAPACITY * 3),
        active: 0,
        life: 0,
        maxLife: 1,
        gravity: 0,
        drag: 0,
    }
    return points
}

const _spawn = {
    set(x, y, z, vx, vy, vz) {
        this.x = x; this.y = y; this.z = z
        this.vx = vx; this.vy = vy; this.vz = vz
    },
}

function emit(emitter, cfg, from, to, scale = 1) {
    const { positions, colors, velocities } = emitter.userData
    const spawnFn = MODES[cfg.mode] ?? MODES.burst
    const count = Math.min(cfg.count, EMITTER_CAPACITY)

    const c0 = new THREE.Color(cfg.colors[0])
    const c1 = new THREE.Color(cfg.colors[1])
    const mixed = new THREE.Color()

    for (let i = 0; i < count; i++) {
        spawnFn(i, count, cfg, from, to, _spawn)
        const i3 = i * 3
        positions[i3] = _spawn.x
        positions[i3 + 1] = _spawn.y
        positions[i3 + 2] = _spawn.z
        velocities[i3] = _spawn.vx * scale
        velocities[i3 + 1] = _spawn.vy * scale
        velocities[i3 + 2] = _spawn.vz * scale

        mixed.copy(c0).lerp(c1, Math.random())
        colors[i3] = mixed.r
        colors[i3 + 1] = mixed.g
        colors[i3 + 2] = mixed.b
    }
    for (let i = count; i < EMITTER_CAPACITY; i++) positions[i * 3 + 1] = -9999

    emitter.geometry.attributes.position.needsUpdate = true
    emitter.geometry.attributes.color.needsUpdate = true
    emitter.material.size = cfg.size * scale
    emitter.material.opacity = 1

    const d = emitter.userData
    d.active = count
    d.life = cfg.life
    d.maxLife = cfg.life
    d.gravity = cfg.gravity
    d.drag = cfg.drag
}

function updateEmitter(emitter, dt) {
    const d = emitter.userData
    if (d.life <= 0) return

    const { positions, velocities, active, gravity, drag } = d
    const damp = Math.max(0, 1 - drag * dt)

    for (let i = 0; i < active; i++) {
        const i3 = i * 3
        velocities[i3] *= damp
        velocities[i3 + 1] = velocities[i3 + 1] * damp + gravity * dt
        velocities[i3 + 2] *= damp
        positions[i3] += velocities[i3] * dt
        positions[i3 + 1] += velocities[i3 + 1] * dt
        positions[i3 + 2] += velocities[i3 + 2] * dt
    }
    emitter.geometry.attributes.position.needsUpdate = true

    d.life = Math.max(0, d.life - dt)
    const t = d.life / d.maxLife
    emitter.material.opacity = t * t
    if (d.life === 0) {
        d.active = 0
        for (let i = 0; i < EMITTER_CAPACITY; i++) positions[i * 3 + 1] = -9999
        emitter.geometry.attributes.position.needsUpdate = true
    }
}

export default function BattleStage({ myCard, opponentCard, opponentPlayed, hitKey, events, me }) {
    const mountRef = useRef(null)
    const sceneRef = useRef(null)

    useEffect(() => {
        const mount = mountRef.current
        if (!mount) return

        const scene = new THREE.Scene()
        scene.background = new THREE.Color(0x090d1a)
        scene.fog = new THREE.Fog(0x090d1a, 9, 18)

        const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100)
        camera.position.set(0, 3.0, 7.2)
        camera.lookAt(0, 1.3, -0.3)

        const renderer = new THREE.WebGLRenderer({ antialias: true })
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
        mount.appendChild(renderer.domElement)

        scene.add(new THREE.AmbientLight(0x93a4ff, 0.7))
        const key = new THREE.DirectionalLight(0xffffff, 1.4)
        key.position.set(3, 6, 5)
        scene.add(key)
        const rim = new THREE.PointLight(0xff5a7a, 1.2, 20)
        rim.position.set(-4, 2, -3)
        scene.add(rim)

        const grid = new THREE.GridHelper(30, 30, 0x2a3a5c, 0x18203a)
        scene.add(grid)

        const myPlatform = makePlatform(0x4b8bff)
        myPlatform.position.set(MY_SIDE.x, 0, MY_SIDE.z)
        myPlatform.scale.setScalar(1.3)
        scene.add(myPlatform)

        const oppPlatform = makePlatform(0xff5a7a)
        oppPlatform.position.set(OPP_SIDE.x, 0, OPP_SIDE.z)
        oppPlatform.scale.setScalar(0.85)
        scene.add(oppPlatform)

        const hiddenTexture = makeHiddenTexture()
        const makeSprite = () => {
            const sprite = new THREE.Sprite(
                new THREE.SpriteMaterial({ map: hiddenTexture, transparent: true })
            )
            sprite.scale.set(2.3, 2.3, 1)
            sprite.position.y = 1.45
            return sprite
        }

        const mySprite = makeSprite()
        mySprite.position.set(MY_SIDE.x, MY_SIDE.y, MY_SIDE.z)
        mySprite.scale.set(MY_SIDE.scale, MY_SIDE.scale, 1)
        scene.add(mySprite)

        const oppSprite = makeSprite()
        oppSprite.position.set(OPP_SIDE.x, OPP_SIDE.y, OPP_SIDE.z)
        oppSprite.scale.set(OPP_SIDE.scale, OPP_SIDE.scale, 1)
        scene.add(oppSprite)

        const emitters = Array.from({ length: EMITTER_COUNT }, () => {
            const e = makeEmitter()
            scene.add(e)
            return e
        })
        let emitterCursor = 0

        const clock = new THREE.Clock()
        const shake = []
        let frameId

        const animate = () => {
            frameId = requestAnimationFrame(animate)
            const dt = Math.min(clock.getDelta(), 0.05)
            const t = clock.elapsedTime

            myPlatform.rotation.y += dt * 0.35
            oppPlatform.rotation.y -= dt * 0.35

            mySprite.position.y = MY_SIDE.y + Math.sin(t * 1.6) * 0.09
            oppSprite.position.y = OPP_SIDE.y + Math.sin(t * 1.6 + 1.7) * 0.09

            for (let i = shake.length - 1; i >= 0; i--) {
                const s = shake[i]
                s.t -= dt
                if (s.t <= 0) {
                    s.sprite.position.x = s.x
                    s.sprite.material.color.setHex(0xffffff)
                    shake.splice(i, 1)
                    continue
                }
                s.sprite.position.x = s.x + Math.sin(s.t * 60) * s.t * 0.5
                s.sprite.material.color.setRGB(1, 1 - s.t * 1.2, 1 - s.t * 1.2)
            }

            emitters.forEach((e) => updateEmitter(e, dt))
            renderer.render(scene, camera)
        }
        animate()

        const resize = () => {
            const w = mount.clientWidth || window.innerWidth
            const h = mount.clientHeight || window.innerHeight
            renderer.setSize(w, h, false)
            camera.aspect = w / h
            camera.updateProjectionMatrix()
        }
        resize()
        const observer = new ResizeObserver(resize)
        observer.observe(mount)

        sceneRef.current = {
            mySprite, oppSprite, hiddenTexture, shake,
            play(cfg, from, to, scale) {
                emit(emitters[emitterCursor], cfg, from, to, scale)
                emitterCursor = (emitterCursor + 1) % EMITTER_COUNT
            },
        }

        return () => {
            cancelAnimationFrame(frameId)
            observer.disconnect()
            scene.traverse((obj) => {
                obj.geometry?.dispose?.()
                if (obj.material) {
                    const materials = Array.isArray(obj.material) ? obj.material : [obj.material]
                    materials.forEach((m) => {
                        m.map?.dispose?.()
                        m.dispose()
                    })
                }
            })
            hiddenTexture.dispose()
            renderer.dispose()
            mount.removeChild(renderer.domElement)
            sceneRef.current = null
        }
    }, [])

    useEffect(() => {
        const ctx = sceneRef.current
        if (!ctx) return

        const loader = new THREE.TextureLoader()
        loader.setCrossOrigin('anonymous')
        let cancelled = false

        const apply = (sprite, card, back) => {
            if (!card) {
                sprite.material.map = ctx.hiddenTexture
                sprite.material.needsUpdate = true
                return
            }
            loader.load(spriteUrl(card.pokemonId, back), (texture) => {
                if (cancelled) { texture.dispose(); return }
                texture.colorSpace = THREE.SRGBColorSpace
                const previous = sprite.material.map
                sprite.material.map = texture
                sprite.material.needsUpdate = true
                if (previous && previous !== ctx.hiddenTexture) previous.dispose()
            })
        }

        apply(ctx.mySprite, myCard, true)
        apply(ctx.oppSprite, opponentCard, false)

        return () => { cancelled = true }
    }, [myCard?.pokemonId, opponentCard?.pokemonId])

    useEffect(() => {
        const ctx = sceneRef.current
        if (!ctx || !hitKey || !events?.length) return

        const timers = []
        events.forEach((event, index) => {
            if (event.type === 'faint') return          // 기절은 이펙트 없음

            const attackerIsMe = event.who === me
            const from = attackerIsMe ? MY_SIDE_X : OPP_SIDE_X
            const to = attackerIsMe ? OPP_SIDE_X : MY_SIDE_X
            const cfg = effectFor(event.moveType, event.moveId)

            timers.push(setTimeout(() => {
                const missed = event.type === 'miss'
                const mult = event.effectiveness ?? 1
                const scale = missed ? 0.45
                    : mult === 0 ? 0.3
                        : mult >= 2 ? 1.35
                            : mult <= 0.5 ? 0.7 : 1
                ctx.play(cfg, from, to, scale)

                if (!missed && event.damage > 0) {
                    const victim = attackerIsMe ? ctx.oppSprite : ctx.mySprite
                    ctx.shake.push({ sprite: victim, t: 0.35, x: to })
                }
            }, index * 900))
        })

        return () => timers.forEach(clearTimeout)
    }, [hitKey, events, me])

    useEffect(() => {
        const ctx = sceneRef.current
        if (!ctx || opponentCard) return
        ctx.oppSprite.material.opacity = opponentPlayed ? 1 : 0.45
    }, [opponentPlayed, opponentCard])

    return <div ref={mountRef} className="stage" />
}