import type React from 'react';
import { Calendar, Music } from 'lucide-react';

import type { Album } from '../../types/api.types';

interface AlbumCardProps {
  album: Album;
  actions?: React.ReactNode;
}

export function AlbumCard({ album, actions }: AlbumCardProps) {
  return (
      <div className="rounded-xl border bg-white p-4 shadow-sm">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <Music className="h-5 w-5 text-slate-600" />
              <h4 className="truncate text-base font-semibold text-slate-900">
                {album.title}
              </h4>
            </div>

            {album.releaseYear ? (
                <div className="mt-1 flex items-center gap-2 text-sm text-slate-600">
                  <Calendar className="h-4 w-4" />
                  <span>{album.releaseYear}</span>
                </div>
            ) : null}
          </div>

          {actions ? <div className="shrink-0">{actions}</div> : null}
        </div>
      </div>
  );
}
