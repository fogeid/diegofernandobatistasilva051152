import { Music2, Users, Play } from 'lucide-react';
import type { Artist } from '../../types/api.types';

export interface ArtistCardProps {
  artist: Artist;
  albumsCount: number;
  onOpen: (id: number) => void;
  animationDelayMs?: number;
}

export function ArtistCard({
                             artist,
                             albumsCount,
                             onOpen,
                             animationDelayMs = 0,
                           }: ArtistCardProps) {
  return (
      <div
          className="group relative cursor-pointer rounded-lg bg-[#181818] p-4 spotify-card animate-fade-in"
          onClick={() => onOpen(artist.id)}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') onOpen(artist.id);
          }}
          style={{ animationDelay: `${animationDelayMs}ms` }}
      >
        <div className="relative mb-4 aspect-square">
          <div className="flex h-full w-full items-center justify-center rounded-full bg-gradient-to-br from-[#1DB954]/20 to-[#282828] shadow-2xl">
            {artist.isBand ? (
                <Users className="h-16 w-16 text-[#1DB954]" />
            ) : (
                <Music2 className="h-16 w-16 text-[#1DB954]" />
            )}
          </div>

          <button
              type="button"
              className="play-button absolute bottom-2 right-2 flex h-12 w-12 items-center justify-center rounded-full bg-[#1DB954] shadow-xl transition-all hover:scale-110 hover:bg-[#1ed760]"
              onClick={(e) => {
                e.stopPropagation();
                onOpen(artist.id);
              }}
              aria-label={`Abrir ${artist.name}`}
          >
            <Play className="ml-0.5 h-5 w-5 fill-black text-black" />
          </button>
        </div>

        <div className="space-y-1">
          <h3 className="truncate font-bold text-white group-hover:underline">
            {artist.name}
          </h3>

          <p className="truncate text-sm text-[#b3b3b3]">
            {artist.isBand ? 'Banda' : 'Artista'} • {albumsCount}{' '}
            {albumsCount === 1 ? 'álbum' : 'álbuns'}
          </p>
        </div>
      </div>
  );
}
